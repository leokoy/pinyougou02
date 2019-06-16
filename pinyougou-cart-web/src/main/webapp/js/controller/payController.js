var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        entity: {},
        payObject:{code_url:'',total_fee:0,out_trade_no:''},//支付的对象
        ids: [],
        searchEntity: {}
    },
    methods: {
        //用于生产支付二维码
        createNative:function () {
            axios.get('/pay/createNative.shtml').then(
                function (response) {//Map
                    app.payObject=response.data;

                    //使用qriou来创建
                    new QRious({
                        element:document.getElementById("qrious"),
                        size:250,
                        level:'H',
                        value:app.payObject.code_url
                    })

                    //发送请求
                    app.queryStatus(app.payObject.out_trade_no);
                }
            )
        },
        //根据订单号查询该订单号的支付的状态
        queryStatus:function (out_trade_no) {
            axios.get('/pay/queryStatus.shtml?out_trade_no='+out_trade_no).then(
                function (response) {//result
                    if(response.data.success){
                        //支付成功
                       window.location.href="paysuccess.html?money="+app.payObject.total_fee;
                    }else{
                        //有两种情况

                        if(response.data.message=='支付超时'){
                            //1.设置 二维码过期，调用微信的关闭订单的接口
                            //2. 直接生成新的二维码 替换掉原来的二维码 并且 关闭之前的的订单。 采用这种。
                            app.createNative();
                        }else{
                           window.location.href="payfail.html";
                        }
                    }
                }
            )
        }
    },
    created: function () {


        this.createNative();
    }
});