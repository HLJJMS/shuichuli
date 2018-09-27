package wlm.watertreatme.bean;

/**
 * Created by wlm on 2018/4/4.
 */

public class MsgCodeBean {


    /**
     * code : 0
     * data : null
     * message : 发送成功
     * myDynamicData : {"tel":"1FDC752C9F18FC01477EA170F6877535","yzm":"122312D575F49373A7CBEC1661794703"}
     */

    private String code;
    private Object data;
    private String message;
    private MyDynamicDataBean myDynamicData;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MyDynamicDataBean getMyDynamicData() {
        return myDynamicData;
    }

    public void setMyDynamicData(MyDynamicDataBean myDynamicData) {
        this.myDynamicData = myDynamicData;
    }

    public static class MyDynamicDataBean {
        /**
         * tel : 1FDC752C9F18FC01477EA170F6877535
         * yzm : 122312D575F49373A7CBEC1661794703
         */

        private String tel;
        private String yzm;

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public String getYzm() {
            return yzm;
        }

        public void setYzm(String yzm) {
            this.yzm = yzm;
        }
    }
}
