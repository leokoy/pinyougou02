var app = new Vue({
    el: "#app",
    data: {
        pages:  15,
        pageNo: 1,
        list: [],
        entity: {},
        ids: [],
        searchEntity: {},

        status: ['','', '', '未发货','已发货']

    },
    methods: {
        searchList:function (curPage) {
            axios.post('/order/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.list=response.data.list;

                //当前页
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            });
        },
        updateStatus: function (status) {
            axios.post('/order/updateStatus.shtml?status=' + status, this.ids).then(
                function (response) {
                    if (response.data.success) {
                        app.ids = [];
                        app.searchList(1);
                    }
                }
            )
        }

    },


    //钩子函数 初始化了事件和
    created: function () {
        this.searchList(1);

    }

})
