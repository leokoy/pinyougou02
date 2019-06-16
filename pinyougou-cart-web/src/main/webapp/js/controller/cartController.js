var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        addressList:[],
        address:{},//存储当前的点击算中的地址对象
        entity: {},
        order:{paymentType:"1"},//定义订单对象 用于绑定页面上所有需要提交的数据。
        totalMoney: 0,
        totalNum: 0,
        ids: [],
        searchEntity: {}
    },
    methods: {
        //方法 就是当页面加载的时候调用 发送请求获取购物车列表数据  赋值给变量  页面绑定变量 循环遍历
        findCartList: function () {
            this.totalMoney = 0;
            this.totalNum = 0;

            axios.get('/cart/findCartList.shtml').then(
                function (response) {//List<Cart>
                    app.list = response.data;

                    for (var i = 0; i < app.list.length; i++) {
                        var obj = app.list[i];//Cartduixiag

                        for (var j = 0; j < obj.orderItemList.length; j++) {
                            var orderItem = obj.orderItemList[j];//
                            app.totalNum += orderItem.num;//数量累计
                            app.totalMoney += orderItem.totalFee;//金额
                        }
                    }
                }
            )
        },
        //向已有的购物中添加商品
        addGoodsToCartList: function (itemId, num) {
            axios.get('/cart/addGoodsToCartList.shtml', {
                params: {
                    //参数名：参数值
                    itemId: itemId,
                    num: num
                }
            }).then(
                function (response) {//result
                    if (response.data.success) {//
                        app.findCartList();
                    } else {
                        alert(response.data.message);
                    }
                }
            )
        },//获取当前的登录的用户的地址列表
        findAddressList:function () {
            axios.get('/address/findAddressListByUserId.shtml').then(
                function (response) {//List<address>
                    app.addressList=response.data;//
                    //循环遍历 查询 默认的地址的对象 赋值给变量
                    for(var i=0;i<app.addressList.length;i++){
                        if(app.addressList[i].isDefault=='1'){
                            app.address=app.addressList[i];
                            break;
                        }
                    }
                }
            )
        },
        //点击的时候调用 影响变量的值 选中地址
        selectAddress:function (address) {
                this.address=address;
        },
        isSelected:function (address) {
            if(this.address==address){
                return true;
            }else{
                return false;
            }
        },
        //点击支付方式的调用改变支付的方式的属性的值
        selectPayType:function (type) {

            this.$set(this.order,'paymentType',type);
            //this.order.paymentType=type;
        },
        //提交订单
        submitOrder:function () {
            this.$set(this.order,'receiverAreaName',this.address.address);
            this.$set(this.order,'receiverMobile',this.address.mobile);
            this.$set(this.order,'receiver',this.address.contact);

            axios.post('/order/add.shtml',this.order).then(
                function (response) {//result
                    if(response.data.success){
                        //提交订单成功
                        window.location.href="pay.html";
                    }else{
                        alert("错误");
                    }
                }
            )
        }


    },
    created: function () {
        this.findCartList();

        //页面加载的时候调用
        if(window.location.href.indexOf("getOrderInfo.html")!=-1)
            this.findAddressList();
    }
});