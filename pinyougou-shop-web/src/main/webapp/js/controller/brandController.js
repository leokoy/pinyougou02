var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        //品牌列表
        list: [],
        entity: {},
        ids: [],
        //搜索条件品牌列表
        searchEntity: {status: '1'},
        //审核状态 0 1 2
        status: ['未审核', '已审核', '审核未通过'],
        ids: [],//用于存储选中的id的值的数组
        //品牌审核
        searchEntityAudit: {}
    },
    methods: {
        /**
         * 苏红霖 2019.6.14
         * 描述：查询商家申请的品牌
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
        //该方法只要不在生命周期的
        add: function () {
            axios.post('/brand/add.shtml', this.entity).then(function (response) {
                console.log(response);
                //isSuccess
                if (response.data.success) {//result:{ isSuccess,}
                    app.searchListAudit(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update: function () {
            axios.post('/brand/update.shtml', this.entity).then(function (response) {
                console.log(response);
                if (response.data.success) {
                    app.searchListAudit(1);
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
                    app.searchListAudit(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
    },
    //钩子函数 初始化了事件和
    created: function () {
        this.searchListAudit(1);
    }

})
