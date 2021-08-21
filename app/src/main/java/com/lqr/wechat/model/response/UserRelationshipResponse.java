package com.lqr.wechat.model.response;

import java.util.List;

/**
 * Created by AMing on 16/1/7.
 * Company RongCloud
 */
public class UserRelationshipResponse {


    /**
     * code : 200
     * result : [{"displayName":"linqiarui2","message":"","status":20,"updatedAt":"2017-04-02T05:06:15.000Z","user":{"id":"2RhVU9aR1","nickname":"linqiarui","region":"86","phone":"15622364316","portraitUri":"http://7xogjk.com1.z0.glb.clouddn.com/Fo7tMdbqSyl5t3QC4Xx9tEQowRdG"}},{"displayName":"","message":"","status":20,"updatedAt":"2017-04-15T03:35:39.000Z","user":{"id":"jJPZIQNJn","nickname":"小嚎哈","region":"86","phone":"13128896442","portraitUri":""}},{"displayName":"","message":"","status":20,"updatedAt":"2017-04-16T05:42:53.000Z","user":{"id":"nmblERhmT","nickname":"合理","region":"86","phone":"18577494191","portraitUri":""}},{"displayName":"","message":"已经start啦","status":20,"updatedAt":"2017-04-17T09:21:50.000Z","user":{"id":"7MGuZVHMI","nickname":"FynnJason","region":"86","phone":"13689619679","portraitUri":"http://7xogjk.com1.z0.glb.clouddn.com/Fl7fwZhserZIkuMAjBp3Fcp7GYEO"}},{"displayName":"","message":"大神，我们交流一下","status":11,"updatedAt":"2017-04-17T10:40:55.000Z","user":{"id":"sNSu362Ns","nickname":"刘伟","region":"86","phone":"18202335430","portraitUri":""}}]
     */

    private int code;
    private List<ResultEntity> result;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<ResultEntity> getResult() {
        return result;
    }

    public void setResult(List<ResultEntity> result) {
        this.result = result;
    }

    public static class ResultEntity {
        /**
         * displayName : linqiarui2
         * message :
         * status : 20
         * updatedAt : 2017-04-02T05:06:15.000Z
         * user : {"id":"2RhVU9aR1","nickname":"linqiarui","region":"86","phone":"15622364316","portraitUri":"http://7xogjk.com1.z0.glb.clouddn.com/Fo7tMdbqSyl5t3QC4Xx9tEQowRdG"}
         */

        private String displayName;
        private String message;
        private int status;
        private String updatedAt;
        private UserEntity user;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public UserEntity getUser() {
            return user;
        }

        public void setUser(UserEntity user) {
            this.user = user;
        }

        public static class UserEntity {
            /**
             * id : 2RhVU9aR1
             * nickname : linqiarui
             * region : 86
             * phone : 15622364316
             * portraitUri : http://7xogjk.com1.z0.glb.clouddn.com/Fo7tMdbqSyl5t3QC4Xx9tEQowRdG
             */

            private String id;
            private String nickname;
            private String region;
            private String phone;
            private String portraitUri;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getNickname() {
                return nickname;
            }

            public void setNickname(String nickname) {
                this.nickname = nickname;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public String getPortraitUri() {
                return portraitUri;
            }

            public void setPortraitUri(String portraitUri) {
                this.portraitUri = portraitUri;
            }
        }
    }
}
