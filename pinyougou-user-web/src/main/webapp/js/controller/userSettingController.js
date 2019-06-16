//品优购用户中心-个人信息
var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        entity: {},
        ids: [],
        smsCode: '',
        username: '',
        searchEntity: {},
        birthday:{year:1990,month:1,day:1},//生日
        imgi:''//保存图片地址
    },
    methods: {
        //方法 是用于图片上传使用 点击上传的按钮的时候调用
        uploadFile:function () {
            //创建一个表单的对象
            var formData=new FormData();

            //添加字段    formData.append('file'           ==> <input type="file"  name="file" value="文件本身">
            //            file.files[0]    第一个file 指定的时候 标签中的id   后面的files[0] 表示获取 选中的第一张文件 对象。File
            formData.append('file', file.files[0]);

            axios({
                url: 'http://localhost:9110/upload/uploadFile.shtml',
                //数据  表单数据
                data: formData,
                method: 'post',
                //设置表单提交的数据类型
                headers: {
                    'Content-Type': 'multipart/form-data'
                },
                //开启跨域请求携带相关认证信息
                withCredentials:true
            }).then(function (response) {
                //文件上传成功
                if(response.data.success){
                    app.entity.headPic=response.data.message;
                }else{
                    //上传失败
                    alert(response.data.message);
                }
            })
        },
        //点击修改,给生日赋值birthday
        updateBirthday:function(){
            var date = app.entity.birthday;
            if (date!=null || date!=undefined){
                //把字符串截取成数组
                var timearr = date.replace(" ", ":").replace(/\:/g, "-").split("-");
                var year=app.birthday.year;
                var month=app.birthday.month;
                var day=app.birthday.day;
                var daa=year+"-"+month+"-"+day+" 00:00:00";
                app.entity.birthday=new Date(daa)
            }

        },
        update: function () {

            axios.post('/user/update.shtml', this.entity).then(function (response) {
                alert(response.data.message);
                app.one();

            }).catch(function (error) {
                console.log("更新失败");
            });
        },
        save: function () {
            if (this.entity.id != null) {
                this.update();
            } else {
                this.add();
            }
        },
        findOne: function (id) {
            axios.get('/user/findOne/' + id + '.shtml').then(function (response) {
                app.entity = response.data;
            }).catch(function (error) {
                console.log("查询单个数据失败");
            });
        },
        dele: function () {
            axios.post('/user/delete.shtml', this.ids).then(function (response) {
                console.log(response);
                if (response.data.success) {
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("删除失败");
            });
        },
        createSmsCode: function () {
            axios.post('user/sendCode.shtml?phone=' + this.entity.phone).then(function (response) {
                if (response.data.success) {
                    alert(response.data.message);
                } else {
                    //验证码发送失败
                    alert(response.data.message);
                }
            }).catch(function (error) {
                console.log("验证码获取失败")
            });
        },
        //根据用户名查找用户信息,用于页面回显
        one: function () {
            axios.get('/user/one.shtml').then(function (response) {
                app.entity = response.data;
                app.imgi = response.data.headPic
                var date = app.entity.birthday;
                //把字符串截取成数组
                var timearr = date.replace(" ", ":").replace(/\:/g, "-").split("-");
                //不需要0几,所有先把字符串转换成int整型,
                app.birthday.year=parseInt(timearr[0]);
                app.birthday.month=parseInt(timearr[1]);
                app.birthday.day=parseInt(timearr[2]);


            }).catch(function (error) {
                console.log("查询单个数据失败");
            });
        },
        getName: function () {
            axios.get('/user/getName.shtml').then(function (response) {
                app.username = response.data;

                app.one()

            }).catch(function (error) {
                console.log("获取用户名失败")
            });
        }
    },
    //钩子函数 初始化了事件和
    created: function () {
        this.getName();
    }
});
