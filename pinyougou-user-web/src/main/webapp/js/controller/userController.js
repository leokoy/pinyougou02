var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{},
        smsCode:'',
        ids:[],
        searchEntity:{}
    },
    methods: {

        //该方法注册用户的
        add:function () {
            axios.post('/user/add/'+this.smsCode+'.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    alert("要去登录");
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        //目的就是 当点击 发短信验证码的时候 调用
        sendSmsCode:function () {
            axios.get('/user/sendSmsCode.shtml?phone='+this.entity.phone).then(
                function (response) {
                    alert(response.message);
                }
            )
        }
    },
    //钩子函数 初始化了事件和
    created: function () {
      


    }

})
