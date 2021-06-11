# ī����� API �ý���

������û�� �޾� ī���� ����ϴ� �������̽��� �����ϴ� Restful API �����ý��� 



## ����

* [����ȯ��](#����ȯ��)
* [���̺� ����](#���̺���)
* [���� ���](#������)
* [�����ذ� ����](#�����ذ�����)
* [����ڵ� ����](#����ڵ�����)
* [���� �׽�Ʈ](#�����׽�Ʈ)
* [�����ڵ� ����](#�����ڵ�����)
* [�׽�Ʈ](#�׽�Ʈ)



## ����ȯ��

1. BackEnd
    * Spring boot
       * Spring ����ȯ�� ����ð��� ���̰� Embedded Tomcat�� Ȱ���ϱ� ���� �뵵
    * JPA/Hibernate
       * ORM����� Ȱ���Ͽ� DB�ڵ鸵 ���Ǽ��� ���� ���
    * Maven
       * Build �� ���̺귯�� ���� �뵵

2. DB
    * H2
       * In-memoery����� ���߰� ȯ�� ���� �뵵
    * Redis
       * Redis�� ������ Ư���� ���� �Լ�(setIfAbsent)�� �̿��Ͽ� �л�LOCK�� ����ϱ� ���� �뵵

3. IDE
    * SpringToolSuite4 : Spring���� ȯ�� 
    * Insomnia : API �׽�Ʈ �뵵



## ���̺�&nbsp;����

1. �������� ���̺�
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
    COMMENT ON TABLE PAYMENT IS '��������';
    COMMENT ON COLUMN PAYMENT.MNG_ID IS '������ȣ';
    COMMENT ON COLUMN PAYMENT.STATUS IS '����/��� ����';
    COMMENT ON COLUMN PAYMENT.PRICE IS '����/��� �ݾ�';
    COMMENT ON COLUMN PAYMENT.VAT IS '����/��� �ΰ���ġ��';
    COMMENT ON COLUMN PAYMENT.INSTALL_MONTHS IS '�Һΰ�����';
    COMMENT ON COLUMN PAYMENT.ENCRYPT_CARD_INFO IS '��ȣȭ�� ī������';
```

2. ī��� ���̺�
```
    CREATE TABLE CARD_COMPANY
    (
    	TRANSACTION_ID               VARCHAR(500) NOT NULL ,
      PRIMARY KEY (TRANSACTION_ID)
    );
    COMMENT ON TABLE CARD_COMPANY IS 'ī���';
    COMMENT ON COLUMN AD_DOC_INFO.TRANSACTION_ID IS 'Ʈ����� ID';
```



## ����&nbsp;���

1. Command Line ����
    ```
    $gradlew build
    ```

2. SpringToolSuite4 ����
    ```
    Application ����
    ```



## �����ذ�&nbsp;����

#### 1. �����ϴ� String ������ (Ʈ�����ID)

 * �������
     * __StringBuffer__�� Ȱ���Ͽ� mutable Object ������� ����(Threadsafe)

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

 * ������Ÿ�� ����
   * __String.format__�� ���� ������Ÿ�� ������

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



#### 2. �ΰ���ġ��

* �ΰ���ġ�� �ڵ����

  * �⺻������ ���׿����ڸ� Ȱ���Ͽ� �ΰ���ġ�� �ڵ� ���

  ~~~java
  return vat == null ? Math.round(price/11) : vat;    
  ~~~

    * ������ҽ� �����ݾ��� ���ų� Request���� �ΰ���ġ���� ���� ��� �ڵ����� ���� �ΰ���ġ���� ȯ��

  ~~~java
  if(remainPrice == 0 && cancelInfo.getVat() == null)
  	return payment.getVat();
  ~~~

* �ΰ���ġ�� ��������

  * __ValidationService.java__ ����Service���� �ΰ���ġ���� ���� ��ȿ�� üũ

  * Case�� ����ó��

    * case #1. ������� �ݾ׺��� ����VAT�� �� ū ���

    ~~~java
    if(cancelInfo.getPrice() < cancelInfo.getVat())
    	throw new CustomException(ExceptionType.PRICE_CANCELRANGE_ERROR);
    ~~~

    * case #2. ���� VAT���� ������� VAT�� �� ū ���

    ~~~java
    if(cancelInfo.getVat() > payment.getVat())
    	throw new CustomException(ExceptionType.PRICE_VATRANGE_ERROR);
    ~~~

    * case #3. ������ҽ� ���� �ݾ׺��� ���� VAT�� �� ū ���

    ~~~java
    Long remainPrice = payment.getPrice() - cancelInfo.getPrice();
    Long remainVAT = payment.getVat() - cancelInfo.getVat();
    if(remainVAT > remainPrice)
    	throw new CustomException(ExceptionType.PRICE_PAYLICK_ERROR);
    ~~~

    

#### 3. ī������

* ��ȣȭ/��ȣȭ ���

  * __javax.crypto.Cipher__ ���̺귯�� Ȱ��
  * __AES__ : ��ȣȭ �˰���
  * __������__ : "_"(�����)

  ~~~properties
  cipher:
    algorithm: AES
    secretkey: kakaopaykakaopay
    separator: _
    charset: UTF-8
  ~~~

* ��ȣȭ

  * ī���ȣ, ��ȿ�Ⱓ, CVC������ ������("_")�� �����Ͽ� ���ڿ��� ����
  * __cipher.doFinal__ �޼��带 ���� ���ڿ��� ����Ʈ �迭�� ��ȯ
  * __Base64.encodeBase64__ �޼��带 ���� UTF-8 Charset���� ��ȯ

  ~~~java
  return new String(Base64.encodeBase64(cipher.doFinal(combineCardInfo.getBytes())), charset);
  ~~~

* ��ȣȭ

  * ��ȣȭ�� �ݴ������ ��ȣȭ (decodeBase64 > cipher.doFinal)
  * __split__ ����� ���� ��ȣȭ�� ���ڿ��� �����ڷ� ������ String�迭�� ��ȯ

  ~~~java
  byte[] encryptBytes = Base64.decodeBase64(encryptCardInfo.getBytes());			String splits[] = new String(cipher.doFinal(encryptBytes), charset).split(separator);
  ~~~

* ����ŷ

  * __StringBuilder__ �� Ȱ���Ͽ� �� 6�ڸ�, �� 3�ڸ��� ������ ������ ī������ ����ŷ
  
  ~~~java
  return new StringBuilder()
  			.append(StringUtils.substring(cardNo, 0, forward))
  			.append(StringUtils.repeat('*', cardNo.length()-forward-backward))
  			.append(StringUtils.substring(cardNo, cardNo.length()-backward, cardNo.length()))
  			.toString();
  ~~~
  
  

#### 4. Ʈ����� ����

* ������ȣ �������

  * Ʈ������� �߻��� ��¥�ð��� ������ �����Ͽ� ������ȣ ����
  * __RandomStringUtils.random__ �޼��带 Ȱ���Ͽ� ���� ����

  ~~~java
  return new StringBuffer()
      	.append(new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()))			
      	.append(RandomStringUtils.random(5, "0123456789abcdefghijklmnopqrstuvwxyzABCDEFHIJKLMNOPSRQTUVWXYZ"))						.toString();
  ~~~

  

* ���������� ����������� ���� (Self-Join)

  * __payMngId__ �÷��� ���� SELF-JOIN ���� ����
  * ������� Ʈ������� ��� __payMngId__ �÷��� ���� Ʈ����� ������ȣ ���� (���� Ʈ������� null�� ����)

  ~~~java
  @OneToMany(mappedBy="payMngId")
  private Set<Payment> subPayMngId;
  
  @ManyToOneprivate 
  Payment payMngId;
  ~~~
  
  


## ����ڵ�&nbsp;����
#### 1. ���ü� ��� ���� ������
* UniqueKey �������
 * ���� &emsp;&emsp;: &emsp;type : ```Payment``` &emsp;/&emsp;value : ```ī���ȣ```
 * ������� : &emsp;type : ```Cancel``` &emsp;/ &emsp;value : ```������ȣ```
```java
String.format("%s::%s", type, value.toString());
```

* Ʈ����� Lock
 * setIfAbsent() �Լ��� ���� �̹� UniqueKey�� ��ϵǾ��ٸ� �������� throw
 * ����/������� Ʈ����ǿ� ���� ���������� UniqueKey�� �����ϱ� ������ �л�Lock ���� ����
```java
 boolean isUnLocked = redisTemplate.opsForValue().setIfAbsent(uniqueKey, "LOCK", 1, TimeUnit.MINUTES);
 if(isUnLocked == false)
	 throw new CustomException(ExceptionType.LOCK_CANCEL_ERROR);
```

* Ʈ����� Unlock
 * Redis�� ����� Lock���� ����
```java
redisTemplate.delete(uniqueKey);
```

#### 2. �ϳ��� ī���ȣ�� ���ÿ� ������ ����ڵ�
 * ```getUniqueKey``` ������ �Լ��� ���� UniqueKey�� �߱޹޴´�.
 * ```paymentLock``` ������ �Լ��� ���� ����Process ������ Lock�� �ɾ�д�.
```java
 paymentKey = lockProvider.getUniqueKey(PaymentType.PAYMENT, paymentInfo.getCardNo());
 lockProvider.paymentLock(paymentKey);
```
 * ���� Process�� �Ϸ�Ǹ� ```unlock``` ������ �Լ��� ����  �ɾ�ξ��� Lock�� �����Ѵ�.
```java
 lockProvider.unlock(paymentKey);
```

#### 3. ���� �� �ǿ� ���� ��ü���(�κ����)�� ����ڵ�
###### _��ü��ҿ� �κ���� ����� �ϳ��� �޼���� �����Ͽ� ������ ���о��� ����ڵ� �ۼ�_
 * ```getUniqueKey``` ������ �Լ��� ���� UniqueKey�� �߱޹޴´�.
 * ```paymentLock``` ������ �Լ��� ���� ������� Process ������ Lock�� �ɾ�д�.
```java
 cancelKey = lockProvider.getUniqueKey(PaymentType.CANCEL, cancelInfo.getMngId());
 lockProvider.cancelLock(cancelKey);
```
 * ������� Process�� �Ϸ�Ǹ� ```unlock``` ������ �Լ��� ���� �ɾ�ξ��� Lock�� �����Ѵ�.
```java
 lockProvider.unlock(cancelKey);
```

#### 4. ����ڵ� ����
 * Junit Test�� ���� ����ڵ带 ����
 * Multi-Thread�� �����Ͽ� ���ÿ� ���� �Ǵ� ������� �޼��忡 �����Ͽ� LOCK�� ���� Exception�� Response�Ǵ��� Ȯ��
 * Multi-Thread ����
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



## ����&nbsp;�׽�Ʈ

* Junit5 �������
* TestCase�� ����
 * TestCase�� �ʿ��� ��������(Request Parameter, ID���� ��) ����
```
CommonTest.java
```
 * ����, �������, �������� ��ȸ TestCase
```
PaymentTest.java
```
 * ���� ��Ȳ�� TestCase
```
ExceptionTest.java
```
 * ���ü����� TestCase
```
MultiThreadTest.java
```



## �����ڵ�&nbsp;����

1. ������Ȳ�� Response Object
```json
{
        "timeStamp": YYYY-MM-DD HH-MM-SS,
        "status": �����ڵ�,
        "msg": �����޼���
}
```

2. �����ڵ� ����

###### _���Ȼ� �����ڵ�� ���� �������� �ʴ� ���� ��Ģ������ �����ý��� �󿡴� ���� ǥ��_

* ���� ���ο���
```json
SERVER_ERROR            code: "001"     message: "����"
```
* Request Parameter ����
```json
PARAM_NULL_ERROR        code: "101"     message: "�ʼ� ��û������ �����Ͽ����ϴ�."
PARAM_LENS_ERROR        code: "102"     message: "���� ���̸� ������ϴ�."
PARAM_DATE_ERROR        code: "103"     message: "��¥������ �ùٸ��� �ʽ��ϴ�."
PARAM_RANGE_ERROR       code: "104"     message: "���� ���� ������ ������ϴ�."
PARAM_NOID_ERROR        code: "105"     message: "�������� �ʴ� ������ȣ�Դϴ�."
PARAM_NOPAY_ERROR       code: "106"     message: "���������� ���� ������ȣ�Դϴ�."
```
* ���� ���� ����
```json
PRICE_VATRANGE_ERROR        code: "201"     message: "��� �ΰ���ġ���� ���� �ΰ���ġ������ Ŭ �� �����ϴ�."
PRICE_PRICELICK_ERROR       code: "202"     message: "���� �ݾ��� �����մϴ�."
PRICE_PAYRANGE_ERROR        code: "203"     message: "�ΰ���ġ���� ���� �ݾ׺��� Ŭ �� �����ϴ�."
PRICE_CANCELRANGE_ERROR     code: "204"     message: "�ΰ���ġ���� ��� �ݾ׺��� Ŭ �� �����ϴ�."
PRICE_PAYLICK_ERROR         code: "205"     message: "����� ���� �ΰ���ġ���� ���� �ݾ׺��� Ŭ �� �����ϴ�."
```
* ���ü� ����
```json
LOCK_PAYMENT_ERROR         code: "301"     message: "�ϳ��� ī���ȣ�� ���ÿ� ������ �� �����ϴ�."
LOCK_CANCEL_ERROR          code: "302"     message: "�ϳ��� ���������� ���ÿ� ����� �� �����ϴ�."
```
* UniqueID ��������
```json
CRT_MNGID_ERROR             code: "401"     message: "������ȣ ������ �����Ͽ����ϴ�."
CRT_TRANSACTIONID_ERROR     code: "402"     message: "Ʈ����ǹ�ȣ ������ �����Ͽ����ϴ�."
```
* ī������ ��ȣȭ ����
```json
CIPHER_ENCRYPT_ERROR         code: "501"     message: "ī�������� ��ȣȭ�ϴµ� �����Ͽ����ϴ�."
CIPHER_DECRYPT_ERROR         code: "502"     message: "ī�������� ��ȣȭ�ϴµ� �����Ͽ����ϴ�."
```
* ī��� ���� ����
```json
SEND_TRANSACTION_ERROR         code: "601"     message: "ī��翡 ������ �����ϴµ� �����Ͽ����ϴ�."
```



# �׽�Ʈ

#### 1. ��ü��� �׽�Ʈ

* ���� �׽�Ʈ

  * ���� Request

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

  * ���� Response

  ~~~json
  {
    "mngId": "210611183045269213eQ",
    "transactionId": " 446   Payment210611183045269213eQ123456789012        001221111    1000000000001000                                                                                                                                                                                                                                                                                    aa/DLGOsBWmiOeDG/ZBmWhaDqgVJ7O4aNIT3TaBXd58=                                               "
  }
  ~~~

* ������� �׽�Ʈ

  * ������� Request

  ~~~json
  PUT 	/payment
  {
  	"mngId": "210611183045269213eQ",
  	"price": 100000,
  	"vat": 1000
  }
  ~~~

  * ������� Response

  ~~~json
  {
    "mngId": "210611184115539PyUaB",
    "transactionId": " 446    Cancel210611184115539PyUaB123456789012        001221111    1000000000001000210611183045269213eQ                                                                                                                                                                                                                                                                aa/DLGOsBWmiOeDG/ZBmWhaDqgVJ7O4aNIT3TaBXd58=                                               "
  }
  ~~~

* ����/������� ��ȸ �׽�Ʈ

  * ����/������� ��ȸ Request

  ~~~json
  GET 	/payment
  {
  	"mngId": "210611183045269213eQ"
  }
  ~~~
  
  * ����/������� ��ȸ Response
  
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
  
  

#### 2. �κ���� �׽�Ʈ

* ���� �׽�Ʈ

  * ���� Request

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

  * ���� Response

  ~~~json
  {
    "mngId": "210611184714472gnJS0",
    "transactionId": " 446   Payment210611184714472gnJS0123456789012        001221111    1000000000001000                                                                                                                                                                                                                                                                                    aa/DLGOsBWmiOeDG/ZBmWhaDqgVJ7O4aNIT3TaBXd58=                                               "
  }
  ~~~

* ������� �׽�Ʈ

  * ������� Request

  ~~~json
  PUT 	/payment
  {
  	"mngId": "210611184714472gnJS0",
  	"price": 1000,
  	"vat": 100
  }
  ~~~

  * ������� Response

  ~~~json
  {
    "mngId": "210611184758302jadZi",
    "transactionId": " 446    Cancel210611184758302jadZi123456789012        001221111      10000000000100210611184714472gnJS0                                                                                                                                                                                                                                                                aa/DLGOsBWmiOeDG/ZBmWhaDqgVJ7O4aNIT3TaBXd58=                                               "
  }
  ~~~

* ����/������� ��ȸ �׽�Ʈ

  * ����/������� ��ȸ Request

  ~~~json
  GET 	/payment
  {
  	"mngId": "210611184714472gnJS0"
  }
  ~~~

  * ����/������� ��ȸ Response

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

