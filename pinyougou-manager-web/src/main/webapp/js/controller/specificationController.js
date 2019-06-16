﻿var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{specification:{},optionList:[{}]},
        //规格审核通过条件列表
        searchEntity:{status: '1'},
        //审核状态 0 1 2
        status: ['未审核', '已审核', '审核未通过'],
        ids: [],//用于存储选中的id的值的数组
        //规格未审核条件列表
        searchEntityAudit: {status: '0'}
    },
    methods: {
        searchList:function (curPage) {
            axios.post('/specification/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.list=response.data.list;

                //当前页
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            });
        },
        /**
         * 苏红霖 2019.6.13
         * 描述：查询规格未审核列表
         * @param curPage
         */
        searchListAudit: function (curPage) {
            axios.post('/specification/search.shtml?pageNo=' + curPage, this.searchEntityAudit).then(function (response) {
                //获取数据
                app.list = response.data.list;
                //当前页
                app.pageNo = curPage;
                //总页数
                app.pages = response.data.pages;
            });
        },

        //查询所有品牌列表
        findAll:function () {
            console.log(app);
            axios.get('/specification/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data;

            }).catch(function (error) {

            })
        },
         findPage:function () {
            var that = this;
            axios.get('/specification/findPage.shtml',{params:{
                pageNo:this.pageNo
            }}).then(function (response) {
                console.log(app);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data.list;
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            }).catch(function (error) {

            })
        },
        //该方法只要不在生命周期的
        add:function () {
            axios.post('/specification/add.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update:function () {
            axios.post('/specification/update.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        save:function () {
            if(this.entity.specification.id!=null){
                this.update();
            }else{
                this.add();
            }
        },
        findOne:function (id) {
            axios.get('/specification/findOne/'+id+'.shtml').then(function (response) {
                app.entity=response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele:function () {
            axios.post('/specification/delete.shtml',this.ids).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },

        addTableRow:function () {
            this.entity.optionList.push({});//向数组中添加对象
        },
        removeTableRow:function (index) {
            //1.第一个参数 表示 索引号（下标）
            //2.第二个参数 表示要删除的个数
            this.entity.optionList.splice(index,1);
        },
        /**
         * 苏红霖 2019.6.13
         * 描述：更新规格审核状态
         * @param status
         */
        updateStatus: function (status) {
            axios.post('/specification/updateStatus.shtml?status=' + status, this.ids).then(
                function (response) {
                    if (response.data.success) {
                        app.ids = [];
                        app.searchListAudit(1);
                    }
                }
            )
        }

    },
    //钩子函数 初始化了事件和
    created: function () {
        if (window.location.href.indexOf("specifcation_audit.html") != -1) {
            this.searchListAudit(1)
            alert("调用了searchListAudit规格")
        } else {
            this.searchList(1);
            alert("调用了searchList")
        }
    }

})
