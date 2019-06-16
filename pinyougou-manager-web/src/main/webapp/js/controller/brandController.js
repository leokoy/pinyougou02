var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        //品牌列表
        list: [],
        entity: {},
        //搜索条件品牌列表
        searchEntity: {status: '1'},
        //审核状态 0 1 2
        status: ['未审核', '已审核', '审核未通过'],
        ids: [],//用于存储选中的id的值的数组
        //品牌审核
        searchEntityAudit: {status: '0'},

    },
    methods: {
        searchList: function (curPage) {

            axios.post('/brand/search.shtml?pageNo=' + curPage, this.searchEntity).then(function (response) {
                //获取数据
                app.list = response.data.list;

                //当前页
                app.pageNo = curPage;
                //总页数
                app.pages = response.data.pages;
            });
        },
        /**
         * 苏红霖 2019.6.13
         * 描述：查询品牌未审核列表
         * @param curPage
         */
        searchListAudit: function (curPage) {
            axios.post('/brand/search.shtml?pageNo=' + curPage, this.searchEntityAudit).then(function (response) {
                //获取数据
                app.list = response.data.list;
                //当前页
                app.pageNo = curPage;
                //总页数
                app.pages = response.data.pages;
            });
        },

        //查询所有品牌列表
        findAll: function () {
            console.log(app);
            axios.get('/brand/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list = response.data;

            }).catch(function (error) {

            })
        },
        findPage: function () {
            var that = this;
            axios.get('/brand/findPage.shtml', {
                params: {
                    pageNo: this.pageNo
                }
            }).then(function (response) {
                console.log(app);
                //注意：this 在axios中就不再是 vue实例了。
                app.list = response.data.list;
                app.pageNo = curPage;
                //总页数
                app.pages = response.data.pages;
            }).catch(function (error) {

            })
        },
        //该方法只要不在生命周期的
        add: function () {
            axios.post('/brand/add.shtml', this.entity).then(function (response) {
                console.log(response);
                //isSuccess
                if (response.data.success) {//result:{ isSuccess,}
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update: function () {
            axios.post('/brand/update.shtml', this.entity).then(function (response) {
                console.log(response);
                if (response.data.success) {
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
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
            axios.get('/brand/findOne/' + id + '.shtml').then(function (response) {
                app.entity = response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele: function () {
            axios.post('/brand/delete.shtml', this.ids).then(function (response) {
                console.log(response);
                if (response.data.success) {
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        /**
         * 苏红霖
         * 描述：更新品牌审核状态
         * @param status
         */
        updateStatus: function (status) {
            axios.post('/brand/updateStatus.shtml?status=' + status, this.ids).then(
                function (response) {
                    if (response.data.success) {
                        app.ids = [];
                        app.searchListAudit(1);
                    }
                }
            )
        },

    },
    //钩子函数 初始化了事件和
    created: function () {
        if (window.location.href.indexOf("brand_audit.html") != -1) {
            this.searchListAudit(1)
            alert("调用了searchListAudit")
        } else {
            this.searchList(1);
            alert("调用了searchList")
        }
    }

})
