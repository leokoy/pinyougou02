var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        entity: {},
        ids: [],
        //状态 0 1 2
        status: ['冻结', '正常'],
        username: '',
        searchEntity: {},
        count: 0,

        //活跃用户
        userActive: 0,
        //非活跃用户
        userNotActive: 0
    },
    methods: {
        //获取登录名
        getUserInfo: function () {
            axios.get('/user/info.shtml').then(
                function (response) {
                    app.username = response.data;
                }
            )


        },
        //用户列表
        searchList: function (curPage) {
            axios.post('/user/search.shtml?pageNo=' + curPage, this.searchEntity).then(function (response) {
                //获取数据
                app.list = response.data.list;
                //当前页
                app.pageNo = curPage;
                //总页数
                app.pages = response.data.pages;
                //当前时间戳
                var timestamp = Date.parse(new Date());

                for (let i = 0; i < app.list.length; i++) {
                    //用户活跃统计
                    var a = timestamp - app.list[i].lastLoginTime;
                    var days = parseInt(a / (1000 * 60 * 60 * 24));
                    if (days > 7) {
                        app.userNotActive += 1;
                    } else {
                        app.userActive += 1;
                    }

                    //创建时间
                    var createdDate = new Date(app.list[i].created);
                    //最后登陆时间
                    var lastLoginTimeDate = new Date(app.list[i].lastLoginTime);
                    app.list[i].created = createdDate.toLocaleString();
                    app.list[i].lastLoginTime = lastLoginTimeDate.toLocaleString();
                }


            });
        },
        //统计用户数
        findAllCount: function () {
            axios.get('/user/findAll.shtml').then(function (response) {
                app.count = response.data;
            })
        },
        /**
         * 描述：更新用户账户状态
         * @param status
         */
        updateStatus: function (status) {
            axios.post('/user/updateStatus.shtml?status=' + status, this.ids).then(
                function (response) {
                    if (response.data.success) {
                        app.ids = [];
                        app.searchList(1);
                    }
                }
            )
        }


    },
    created: function () {
        this.getUserInfo();
        this.findAllCount();
        this.searchList(1)
    }
});