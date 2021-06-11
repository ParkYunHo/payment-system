# 카드결제 API 시스템

결제요청을 받아 카드사와 통신하는 인터페이스를 제공하는 Restful API 결제시스템 



## 목자

* [개발환경](#개발환경)
* [테이블 설계](#테이블설계)
* [빌드 방법](#빌드방법)
* [문제해결 전략](#문제해결전략)
* [방어코드 증명](#방어코드증명)
* [단위 테스트](#단위테스트)
* [에러코드 정의](#에러코드정의)
* [테스트](#테스트)



## 개발환경

1. BackEnd
    * Spring boot
       * Spring 개발환경 구축시간을 줄이고 Embedded Tomcat을 활용하기 위한 용도
    * JPA/Hibernate
       * ORM기술을 활용하여 DB핸들링 편의성을 위해 사용
    * Maven
       * Build 및 라이브러리 관리 용도

2. DB
    * H2
       * In-memoery방식의 개발계 환경 구축 용도
    * Redis
       * Redis의 원자적 특성을 갖는 함수(setIfAbsent)를 이용하여 분산LOCK을 사용하기 위한 용도

3. IDE
    * SpringToolSuite4 : Spring개발 환경 
    * Insomnia : API 테스트 용도



## 테이블&nbsp;설계

1. 결제정보 테이블
```
    CREATE TABLE PAYMENT
    (
    	MNG_ID               VARCHAR2(255) NOT NULL,
    	STATUS               VARCHAR2(255) NULL,
    	PRICE                BIGINT NULL,
    	VAT                  BIGINT NULL,
    	INSTALL_MONTHS       BIGINT NULL,
    	ENCRYPT_CARD_INFO    VARCHAR(255) NULL,
    	PAY_MNG_ID			 VARCHAR(255) NULL,
      PRIMARY KEY (MNG_ID)
    );
    COMMENT ON TABLE PAYMENT IS '결제정보';
    COMMENT ON COLUMN PAYMENT.MNG_ID IS '관리번호';
    COMMENT ON COLUMN PAYMENT.STATUS IS '결제/취소 상태';
    COMMENT ON COLUMN PAYMENT.PRICE IS '결제/취소 금액';
    COMMENT ON COLUMN PAYMENT.VAT IS '결제/취소 부가가치세';
    COMMENT ON COLUMN PAYMENT.INSTALL_MONTHS IS '할부개월수';
    COMMENT ON COLUMN PAYMENT.ENCRYPT_CARD_INFO IS '암호화된 카드정보';
```

2. 카드사 테이블
```
    CREATE TABLE CARD_COMPANY
    (
    	TRANSACTION_ID               VARCHAR(500) NOT NULL ,
      PRIMARY KEY (TRANSACTION_ID)
    );
    COMMENT ON TABLE CARD_COMPANY IS '카드사';
    COMMENT ON COLUMN AD_DOC_INFO.TRANSACTION_ID IS '트랜잭션 ID';
```



## 빌드&nbsp;방법

1. Command Line 빌드
    ```
    $gradlew build
    ```

2. SpringToolSuite4 빌드
    ```
    Application 실행
    ```



## 문제해결&nbsp;전략

#### 1. 저장하는 String 데이터 (트랜잭션ID)

 * 생성방식
     * __StringBuffer__ 를 활용하여 mutable Object 방식으로 구현(Threadsafe)

~~~java
return new StringBuffer()
	.append(formatter(cardInfo.getCardNo(), IDType.CARD_NO))
	.append(formatter(payment.getInstallMonths(), IDType.INSTALL_MONTHS))
	.append(formatter(cardInfo.getExpiryDate(), IDType.EXPIRY_DATE))
	.append(formatter(cardInfo.getCvc(), IDType.CVC))
	.append(formatter(payment.getPrice(), IDType.PRICE))
	.append(formatter(payment.getVat(), IDType.VAT))
	.append(formatter(payMngId, IDType.PAY_MNG_ID))
	.append(formatter(payment.getCardInfo(), IDType.ENCRYPT_CARD_INFO))
	.append(formatter("", IDType.EXTRA))
	.insert(0, formatter(payment.getMngId(), IDType.MNG_ID))
	.insert(0, formatter(payment.getStatus(), IDType.STATUS))
	.insert(0, formatter(446, IDType.TOTAL_LENS))
	.toString();
~~~

 * 데이터타입 정의
   * __String.format__ 을 통한 데이터타입 선정의

~~~java
switch(type) {
	case NumberType.NUMBER:
		return String.format("%"+lens+"d", Long.parseLong(data));
	case NumberType.NUMBER_O:
		return String.format("%0"+lens+"d", Long.parseLong(data));
	case NumberType.NUMBER_L:
		return String.format("%-"+lens+"d", Long.parseLong(data));
	case NumberType.STRING:
		return String.format("%"+lens+"s", data);
	default:
		throw new CustomException(ExceptionType.CRT_TRANSACTIONID_ERROR);
}
~~~



#### 2. 부가가치세

* 부가가치세 자동계산

  * 기본적으로 삼항연산자를 활용하여 부가가치세 자동 계산

  ~~~java
  return vat == null ? Math.round(price/11) : vat;    
  ~~~

    * 결제취소시 남은금액이 없거나 Request받은 부가가치세가 없는 경우 자동으로 남은 부가가치세로 환산

  ~~~java
  if(remainPrice == 0 && cancelInfo.getVat() == null)
  	return payment.getVat();
  ~~~

* 부가가치세 범위제한

  * __ValidationService.java__ 검증Service에서 부가가치세에 대한 유효성 체크

  * Case별 예외처리

    * case #1. 결제취소 금액보다 결제VAT가 더 큰 경우

    ~~~java
    if(cancelInfo.getPrice() < cancelInfo.getVat())
    	throw new CustomException(ExceptionType.PRICE_CANCELRANGE_ERROR);
    ~~~

    * case #2. 남은 VAT보다 결제취소 VAT가 더 큰 경우

    ~~~java
    if(cancelInfo.getVat() > payment.getVat())
    	throw new CustomException(ExceptionType.PRICE_VATRANGE_ERROR);
    ~~~

    * case #3. 결제취소시 남은 금액보다 남은 VAT가 더 큰 경우

    ~~~java
    Long remainPrice = payment.getPrice() - cancelInfo.getPrice();
    Long remainVAT = payment.getVat() - cancelInfo.getVat();
    if(remainVAT > remainPrice)
    	throw new CustomException(ExceptionType.PRICE_PAYLICK_ERROR);
    ~~~

    

#### 3. 카드정보

* 암호화/복호화 방식

  * __javax.crypto.Cipher__ 라이브러리 활용
  * __AES__ : 암호화 알고리즘
  * __구분자__ : "_"(언더바)

  ~~~properties
  cipher:
    algorithm: AES
    secretkey: kakaopaykakaopay
    separator: _
    charset: UTF-8
  ~~~

* 암호화

  * 카드번호, 유효기간, CVC정보를 구분자("_")로 연결하여 문자열로 저장
  * __cipher.doFinal__ 메서드를 통해 문자열을 바이트 배열로 변환
  * __Base64.encodeBase64__ 메서드를 통해 UTF-8 Charset으로 변환

  ~~~java
  return new String(Base64.encodeBase64(cipher.doFinal(combineCardInfo.getBytes())), charset);
  ~~~

* 복호화

  * 암호화의 반대순서로 복호화 (decodeBase64 > cipher.doFinal)
  * __split__ 기능을 통해 복호화된 문자열을 구분자로 나누어 String배열로 변환

  ~~~java
  byte[] encryptBytes = Base64.decodeBase64(encryptCardInfo.getBytes());			String splits[] = new String(cipher.doFinal(encryptBytes), charset).split(separator);
  ~~~

* 마스킹

  * __StringBuilder__ 를 활용하여 앞 6자리, 뒤 3자리를 제외한 나머지 카드정보 마스킹
  
  ~~~java
  return new StringBuilder()
  			.append(StringUtils.substring(cardNo, 0, forward))
  			.append(StringUtils.repeat('*', cardNo.length()-forward-backward))
  			.append(StringUtils.substring(cardNo, cardNo.length()-backward, cardNo.length()))
  			.toString();
  ~~~
  
  

#### 4. 트랜잭션 관리

* 관리번호 생성방식

  * 트랜잭션이 발생한 날짜시간과 난수를 조합하여 관리번호 생성
  * __RandomStringUtils.random__ 메서드를 활용하여 난수 생성

  ~~~java
  return new StringBuffer()
      	.append(new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()))			
      	.append(RandomStringUtils.random(5, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFHIJKLMNOPSRQTUVWXYZ"))						.toString();
  ~~~

  

* 결제정보와 결제취소정보 연결 (Self-Join)

  * __payMngId__ 컬럼을 통해 SELF-JOIN 관계 설정
  * 결제취소 트랜잭션인 경우 __payMngId__ 컬럼에 결제 트랜잭션 관리번호 저장 (결제 트랜잭션은 null값 저장)

  ~~~java
  @OneToMany(mappedBy="payMngId")
  private Set<Payment> subPayMngId;
  
  @ManyToOneprivate 
  Payment payMngId;
  ~~~
  
  


## 방어코드&nbsp;증명
#### 1. 동시성 제어를 위한 공통기능
* UniqueKey 생성기능
 * 결제 &emsp;&emsp;: &emsp;type : ```Payment``` &emsp;/&emsp;value : ```카드번호```
 * 결제취소 : &emsp;type : ```Cancel``` &emsp;/ &emsp;value : ```관리번호```
```java
String.format("%s::%s", type, value.toString());
```

* 트랜잭션 Lock
 * setIfAbsent() 함수를 통해 이미 UniqueKey가 등록되었다면 에러정보 throw
 * 결제/결제취소 트랜잭션에 대한 고유정보로 UniqueKey를 구성하기 때문에 분산Lock 구현 가능
```java
 boolean isUnLocked = redisTemplate.opsForValue().setIfAbsent(uniqueKey, "LOCK", 1, TimeUnit.MINUTES);
 if(isUnLocked == false)
	 throw new CustomException(ExceptionType.LOCK_CANCEL_ERROR);
```

* 트랜잭션 Unlock
 * Redis에 등록한 Lock정보 삭제
```java
redisTemplate.delete(uniqueKey);
```

#### 2. 하나의 카드번호로 동시에 결제시 방어코드
 * ```getUniqueKey``` 공통기능 함수를 통해 UniqueKey를 발급받는다.
 * ```paymentLock``` 공통기능 함수를 통해 결제Process 시작전 Lock을 걸어둔다.
```java
 paymentKey = lockProvider.getUniqueKey(PaymentType.PAYMENT, paymentInfo.getCardNo());
 lockProvider.paymentLock(paymentKey);
```
 * 결제 Process가 완료되면 ```unlock``` 공통기능 함수를 통해  걸어두었던 Lock을 해제한다.
```java
 lockProvider.unlock(paymentKey);
```

#### 3. 결제 한 건에 대한 전체취소(부분취소)시 방어코드
###### _전체취소와 부분취소 기능을 하나의 메서드로 구현하여 별도의 구분없이 방어코드 작성_
 * ```getUniqueKey``` 공통기능 함수를 통해 UniqueKey를 발급받는다.
 * ```paymentLock``` 공통기능 함수를 통해 결제취소 Process 시작전 Lock을 걸어둔다.
```java
 cancelKey = lockProvider.getUniqueKey(PaymentType.CANCEL, cancelInfo.getMngId());
 lockProvider.cancelLock(cancelKey);
```
 * 결제취소 Process가 완료되면 ```unlock``` 공통기능 함수를 통해 걸어두었던 Lock을 해제한다.
```java
 lockProvider.unlock(cancelKey);
```

#### 4. 방어코드 증명
 * Junit Test를 통해 방어코드를 증명
 * Multi-Thread를 생성하여 동시에 결제 또는 결제취소 메서드에 접근하여 LOCK에 의한 Exception이 Response되는지 확인
 * Multi-Thread 구성
```java
Runnable thread = () -> {
		try {
			Object obj = super.paymentThreadTest(req);
			
			if(obj instanceof CustomException) {
				CustomException exception = (CustomException)obj;
				if(exception.getError() == ExceptionType.LOCK_PAYMENT_ERROR) errorList.add(exception.getError());
			}
		}catch(Throwable e) {}	
	};
CompletableFuture
			.allOf(CompletableFuture.runAsync(thread), CompletableFuture.runAsync(thread))
			.join();
```



## 단위&nbsp;테스트

* Junit5 버전사용
* TestCase별 설명
 * TestCase에 필요한 공통정보(Request Parameter, ID정보 등) 제공
```
CommonTest.java
```
 * 결제, 결제취소, 결제정보 조회 TestCase
```
PaymentTest.java
```
 * 예외 상황별 TestCase
```
ExceptionTest.java
```
 * 동시성제어 TestCase
```
MultiThreadTest.java
```



## 에러코드&nbsp;정의

1. 에러상황시 Response Object
```json
{
        "timeStamp": YYYY-MM-DD HH-MM-SS,
        "status": 에러코드,
        "msg": 에러메세지
}
```

2. 에러코드 정의

###### _보안상 에러코드는 상세히 제공하지 않는 것이 원칙이지만 결제시스템 상에는 상세히 표현_

* 서버 내부에러
```json
SERVER_ERROR            code: "001"     message: "에러"
```
* Request Parameter 에러
```json
PARAM_NULL_ERROR        code: "101"     message: "필수 요청정보를 누락하였습니다."
PARAM_LENS_ERROR        code: "102"     message: "허용된 길이를 벗어났습니다."
PARAM_DATE_ERROR        code: "103"     message: "날짜형식이 올바르지 않습니다."
PARAM_RANGE_ERROR       code: "104"     message: "허용된 값의 범위를 벗어났습니다."
PARAM_NOID_ERROR        code: "105"     message: "존재하지 않는 관리번호입니다."
PARAM_NOPAY_ERROR       code: "106"     message: "결재정보가 없는 관리번호입니다."
```
* 가격 검증 에러
```json
PRICE_VATRANGE_ERROR        code: "201"     message: "취소 부가가치세가 남은 부가가치세보다 클 수 없습니다."
PRICE_PRICELICK_ERROR       code: "202"     message: "남은 금액이 부족합니다."
PRICE_PAYRANGE_ERROR        code: "203"     message: "부가가치세가 결제 금액보다 클 수 없습니다."
PRICE_CANCELRANGE_ERROR     code: "204"     message: "부가가치세가 취소 금액보다 클 수 없습니다."
PRICE_PAYLICK_ERROR         code: "205"     message: "취소후 남은 부가가치세가 남은 금액보다 클 수 없습니다."
```
* 동시성 에러
```json
LOCK_PAYMENT_ERROR         code: "301"     message: "하나의 카드번호로 동시에 결제할 수 없습니다."
LOCK_CANCEL_ERROR          code: "302"     message: "하나의 결제정보를 동시에 취소할 수 없습니다."
```
* UniqueID 생성에러
```json
CRT_MNGID_ERROR             code: "401"     message: "관리번호 생성에 실패하였습니다."
CRT_TRANSACTIONID_ERROR     code: "402"     message: "트랜잭션번호 생성에 실패하였습니다."
```
* 카드정보 암호화 에러
```json
CIPHER_ENCRYPT_ERROR         code: "501"     message: "카드정보를 암호화하는데 실패하였습니다."
CIPHER_DECRYPT_ERROR         code: "502"     message: "카드정보를 복호화하는데 실패하였습니다."
```
* 카드사 전송 에러
```json
SEND_TRANSACTION_ERROR         code: "601"     message: "카드사에 정보를 전송하는데 실패하였습니다."
```



# 테스트

#### 1. 전체취소 테스트

* 결제 테스트

  * 결제 Request

  ~~~json
  POST 	/payment
  {
  	"cardNo": 123456789012,
  	"expiryDate": 1221,
  	"cvc": 111,
  	"installMonths": 0,
  	"price": 100000,
  	"vat": 1000
  }
  ~~~

  * 결제 Response

  ~~~json
  {
    "mngId": "210611183045269213eQ",
    "transactionId": " 446   Payment210611183045269213eQ123456789012        001221111    1000000000001000                                                                                                                                                                                                                                                                                    aa/DLGOsBWmiOeDG/ZBmWhaDqgVJ7O4aNIT3TaBXd58=                                               "
  }
  ~~~

* 결제취소 테스트

  * 결제취소 Request

  ~~~json
  PUT 	/payment
  {
  	"mngId": "210611183045269213eQ",
  	"price": 100000,
  	"vat": 1000
  }
  ~~~

  * 결제취소 Response

  ~~~json
  {
    "mngId": "210611184115539PyUaB",
    "transactionId": " 446    Cancel210611184115539PyUaB123456789012        001221111    1000000000001000210611183045269213eQ                                                                                                                                                                                                                                                                aa/DLGOsBWmiOeDG/ZBmWhaDqgVJ7O4aNIT3TaBXd58=                                               "
  }
  ~~~

* 결제/결제취소 조회 테스트

  * 결제/결제취소 조회 Request

  ~~~json
  GET 	/payment
  {
  	"mngId": "210611183045269213eQ"
  }
  ~~~
  
  * 결제/결제취소 조회 Response
  
  ~~~json
  {
    "mngId": "210611183045269213eQ",
    "status": "Payment",
    "cardInfo": {
      "cardNo": "123456***012",
      "expiryDate": 1221,
      "cvc": 111
    },
    "priceInfo": {
      "price": 0,
      "vat": 0
    }
  }
  ~~~
  
  

#### 2. 부분취소 테스트

* 결제 테스트

  * 결제 Request

  ~~~json
  POST 	/payment
  {
  	"cardNo": 123456789012,
  	"expiryDate": 1221,
  	"cvc": 111,
  	"installMonths": 0,
  	"price": 100000,
  	"vat": 1000
  }
  ~~~

  * 결제 Response

  ~~~json
  {
    "mngId": "210611184714472gnJS0",
    "transactionId": " 446   Payment210611184714472gnJS0123456789012        001221111    1000000000001000                                                                                                                                                                                                                                                                                    aa/DLGOsBWmiOeDG/ZBmWhaDqgVJ7O4aNIT3TaBXd58=                                               "
  }
  ~~~

* 결제취소 테스트

  * 결제취소 Request

  ~~~json
  PUT 	/payment
  {
  	"mngId": "210611184714472gnJS0",
  	"price": 1000,
  	"vat": 100
  }
  ~~~

  * 결제취소 Response

  ~~~json
  {
    "mngId": "210611184758302jadZi",
    "transactionId": " 446    Cancel210611184758302jadZi123456789012        001221111      10000000000100210611184714472gnJS0                                                                                                                                                                                                                                                                aa/DLGOsBWmiOeDG/ZBmWhaDqgVJ7O4aNIT3TaBXd58=                                               "
  }
  ~~~

* 결제/결제취소 조회 테스트

  * 결제/결제취소 조회 Request

  ~~~json
  GET 	/payment
  {
  	"mngId": "210611184714472gnJS0"
  }
  ~~~

  * 결제/결제취소 조회 Response

  ~~~json
  {
    "mngId": "210611184714472gnJS0",
    "status": "Payment",
    "cardInfo": {
      "cardNo": "123456***012",
      "expiryDate": 1221,
      "cvc": 111
    },
    "priceInfo": {
      "price": 99000,
      "vat": 900
    }
  }
  ~~~

