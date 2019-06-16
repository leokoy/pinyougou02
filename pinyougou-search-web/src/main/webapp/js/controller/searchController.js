var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        entity: {},
        searchMap:{'keywords':'','category':'','brand':'',spec:{},'price':'','pageNo':1,'pageSize':40,'sortType':'','sortField':''},//用于绑定搜索的条件的参数的对象
        resultMap:{brandList:[]},//用于接收后台返回出来的map对象
        pageLabels:[],//赋值了  12 345  12345   1345
        ids: [],
        preDotted:false,
        nextDotted:false,
        searchEntity: {}
    },
    methods:{
        searchList:function () {
            axios.post('/itemSearch/search.shtml',this.searchMap).then(
                function (response) {//Map
                     app.resultMap=response.data;

                     //查询之后再调用
                    app.buildPageLabel();
                }
            )

        },
        buildPageLabel:function () {
            this.pageLabels=[];//重新赋值给空值

            var firstPage =1;
            var lastPage = this.resultMap.totalPages;


            //需要判断
            //总页数>5页
            if(this.resultMap.totalPages>5){

                if(this.searchMap.pageNo<=3){
                    //判断  如果当前的页码<=3 那么就显示前5页的数据
                    this.preDotted=false;
                    this.nextDotted=true;
                    firstPage=1;
                    lastPage=5;
                }else if(this.searchMap.pageNo >= this.resultMap.totalPages-2){
                    //判断 如果 当前的页码>=总页数-2 显示后5页的数据
                    firstPage= this.resultMap.totalPages-4;    //100      96 97 98 99 100
                    lastPage=this.resultMap.totalPages;

                    this.preDotted=true;
                    this.nextDotted=false;
                }else{
                    //否则 就显示中间的5页码
                    firstPage= this.searchMap.pageNo-2;
                    lastPage=this.searchMap.pageNo+2;
                    this.preDotted=true;
                    this.nextDotted=true;
                }
            }else{
                //总页数<=5页
                this.preDotted=false;
                this.nextDotted=false;
               firstPage =1;
               lastPage = this.resultMap.totalPages;
            }

            console.log(firstPage);
            console.log(lastPage);


            for(var i=firstPage;i<=lastPage;i++){
                this.pageLabels.push(i);
            }

        },
        //目的就是点击排序的时候调用  改变 变量的值 并搜索
        doSort:function (sortField,sortType) {
            this.searchMap.sortField=sortField;
            this.searchMap.sortType=sortType;
            this.searchList();
        },
        clear:function () {
            this.searchMap={'keywords':this.searchMap.keywords,'category':'','brand':'',spec:{},'price':'','pageNo':1,'pageSize':40,'sortType':'','sortField':''};
        },
        //当点击的时候调用 1.将被点击到的名称 赋值给 变量  2.发送请求重新查询
        addSearchItem:function (key,value) {
            if(key=='category' || key=='brand' || key=='price'){
                this.searchMap[key]=value;
            }else{
                this.searchMap.spec[key]=value;
            }

            this.searchList();
        },
        removeSearchItem:function (key) {
            //删除变量的值
            if(key=='category' || key=='brand'|| key=='price'){
                this.searchMap[key]='';
            }else{
                delete this.searchMap.spec[key];
            }

            //发送请求 重新查询
            this.searchList();

        },
        //判断 搜索的关键字是否 为品牌列表中的品牌
        isKeywordsIsBrand:function () {
            //循环遍历  品牌列表  判断 关键字是否 包含 品牌
            for(var i=0;i<this.resultMap.brandList.length;i++){

                if(this.searchMap.keywords.indexOf(this.resultMap.brandList[i].text)!=-1){
                    //赋值
                    this.searchMap.brand=this.resultMap.brandList[i].text;
                    return true;
                }
            }

            return false;
        },
        //目的就是 当点击页码的时候调用  将被点击到的页码的值赋值给变量pageNO,发送请求 获取数据
        queryByPage:function (page) {
            var number = parseInt(page);
            if(number>this.resultMap.totalPages){
                number=this.resultMap.totalPages;
            }
            if(number<1){
                number=1;
            }
            this.searchMap.pageNo=number;
            this.searchList();
        }


    },
    created:function () {
        //1.获取url中的参数列表值
        var requestObject = this.getUrlParam();//{"keyowrds":"手机",....}

        if(requestObject!=undefined && (requestObject.keywords!=null || requestObject.keywords!=undefined )){
            //2.将参数的值赋值给变量keywords
            this.searchMap.keywords= decodeURIComponent(requestObject.keywords);//解码
            //3.执行搜索的方法
            this.searchList();

        }

    }
});