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
1. ����
2. �������
3. �������� ��ȸ
4. ������


## ����ڵ�&nbsp;����
### 1. ���ü� ��� ���� ������
* UniqueKey �������
 * ���� &emsp;&emsp;: &emsp;type : ```Payment``` &emsp;/&emsp;value : ```ī���ȣ```
 * ������� : &emsp;type : ```Cancel``` &emsp;/ &emsp;value : ```������ȣ```
```
String.format("%s::%s", type, value.toString());
```

* Ʈ����� Lock
 * setIfAbsent() �Լ��� ���� �̹� UniqueKey�� ��ϵǾ��ٸ� �������� throw
 * ����/������� Ʈ����ǿ� ���� ���������� UniqueKey�� �����ϱ� ������ �л�Lock ���� ����
```
 boolean isUnLocked = redisTemplate.opsForValue().setIfAbsent(uniqueKey, "LOCK", 1, TimeUnit.MINUTES);
 if(isUnLocked == false)
	 throw new CustomException(ExceptionType.LOCK_CANCEL_ERROR);
```

* Ʈ����� Unlock
 * Redis�� ����� Lock���� ����
```
redisTemplate.delete(uniqueKey);
```

### 2. �ϳ��� ī���ȣ�� ���ÿ� ������ ����ڵ�
 * ```getUniqueKey``` ������ �Լ��� ���� UniqueKey�� �߱޹޴´�.
 * ```paymentLock``` ������ �Լ��� ���� ����Process ������ Lock�� �ɾ�д�.
```
 paymentKey = lockProvider.getUniqueKey(PaymentType.PAYMENT, paymentInfo.getCardNo());
 lockProvider.paymentLock(paymentKey);
```
 * ���� Process�� �Ϸ�Ǹ� ```unlock``` ������ �Լ��� ����  �ɾ�ξ��� Lock�� �����Ѵ�.
```
 lockProvider.unlock(paymentKey);
```

### 3. ���� �� �ǿ� ���� ��ü���(�κ����)�� ����ڵ�
###### _��ü��ҿ� �κ���� ����� �ϳ��� �޼���� �����Ͽ� ������ ���о��� ����ڵ� �ۼ�_
 * ```getUniqueKey``` ������ �Լ��� ���� UniqueKey�� �߱޹޴´�.
 * ```paymentLock``` ������ �Լ��� ���� ������� Process ������ Lock�� �ɾ�д�.
```
 cancelKey = lockProvider.getUniqueKey(PaymentType.CANCEL, cancelInfo.getMngId());
 lockProvider.cancelLock(cancelKey);
```
 * ������� Process�� �Ϸ�Ǹ� ```unlock``` ������ �Լ��� ���� �ɾ�ξ��� Lock�� �����Ѵ�.
```
 lockProvider.unlock(cancelKey);
```

### 4. ����ڵ� ����
 * Junit Test�� ���� ����ڵ带 ����
 * Multi-Thread�� �����Ͽ� ���ÿ� ���� �Ǵ� ������� �޼��忡 �����Ͽ� LOCK�� ���� Exception�� Response�Ǵ��� Ȯ��
 * Multi-Thread ����
```
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
```
{
        "timeStamp": YYYY-MM-DD HH-MM-SS,
        "status": �����ڵ�,
        "msg": �����޼���
}
```

2. �����ڵ� ����

###### _���Ȼ� �����ڵ�� ���� �������� �ʴ� ���� ��Ģ������ �����ý��� �󿡴� ���� ǥ��_

* ���� ���ο���
```
SERVER_ERROR            code: "001"     message: "����"
```
* Request Parameter ����
```
PARAM_NULL_ERROR        code: "101"     message: "�ʼ� ��û������ �����Ͽ����ϴ�."
PARAM_LENS_ERROR        code: "102"     message: "���� ���̸� ������ϴ�."
PARAM_DATE_ERROR        code: "103"     message: "��¥������ �ùٸ��� �ʽ��ϴ�."
PARAM_RANGE_ERROR       code: "104"     message: "���� ���� ������ ������ϴ�."
PARAM_NOID_ERROR        code: "105"     message: "�������� �ʴ� ������ȣ�Դϴ�."
PARAM_NOPAY_ERROR       code: "106"     message: "���������� ���� ������ȣ�Դϴ�."
```
* ���� ���� ����
```
PRICE_VATRANGE_ERROR        code: "201"     message: "��� �ΰ���ġ���� ���� �ΰ���ġ������ Ŭ �� �����ϴ�."
PRICE_PRICELICK_ERROR       code: "202"     message: "���� �ݾ��� �����մϴ�."
PRICE_PAYRANGE_ERROR        code: "203"     message: "�ΰ���ġ���� ���� �ݾ׺��� Ŭ �� �����ϴ�."
PRICE_CANCELRANGE_ERROR     code: "204"     message: "�ΰ���ġ���� ��� �ݾ׺��� Ŭ �� �����ϴ�."
PRICE_PAYLICK_ERROR         code: "205"     message: "����� ���� �ΰ���ġ���� ���� �ݾ׺��� Ŭ �� �����ϴ�."
```
* ���ü� ����
```
LOCK_PAYMENT_ERROR         code: "301"     message: "�ϳ��� ī���ȣ�� ���ÿ� ������ �� �����ϴ�."
LOCK_CANCEL_ERROR          code: "302"     message: "�ϳ��� ���������� ���ÿ� ����� �� �����ϴ�."
```
* UniqueID ��������
```
CRT_MNGID_ERROR             code: "401"     message: "������ȣ ������ �����Ͽ����ϴ�."
CRT_TRANSACTIONID_ERROR     code: "402"     message: "Ʈ����ǹ�ȣ ������ �����Ͽ����ϴ�."
```
* ī������ ��ȣȭ ����
```
CIPHER_ENCRYPT_ERROR         code: "501"     message: "ī�������� ��ȣȭ�ϴµ� �����Ͽ����ϴ�."
CIPHER_DECRYPT_ERROR         code: "502"     message: "ī�������� ��ȣȭ�ϴµ� �����Ͽ����ϴ�."
```
* ī��� ���� ����
```
SEND_TRANSACTION_ERROR         code: "601"     message: "ī��翡 ������ �����ϴµ� �����Ͽ����ϴ�."
```