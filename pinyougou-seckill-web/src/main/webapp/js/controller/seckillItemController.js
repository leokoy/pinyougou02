var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        timeString:'',//绑定详情页面的倒计时的变量
        entity: {},
        seckillId:0,//秒杀商品的ID
        ids: [],
        messageInfo:'',
        goodsInfo:{count:0},
        searchEntity: {}
    },
    methods:{

        //将毫秒数 转成要的格式。返回
        convertTimeString:function (alltime) {
            //将毫秒数 转成。 10天 01时 30 分 28秒
            var allsecond=Math.floor(alltime/1000);//毫秒数转成 秒数。
            var days= Math.floor( allsecond/(60*60*24));//天数
            var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
            var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
            var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数

            if(days<10){//1
                days="0"+days;
            }

            if(hours<10){//1
                hours="0"+hours;
            }

            if(minutes<10){
                minutes="0"+minutes;
            }

            if(seconds<10){
                seconds="0"+seconds;
            }


            return days+"天"+hours+"小时"+minutes+"分钟"+seconds+"秒";
        },

        //倒计时  参数：倒计时的时间的毫秒数
        caculate:function (time) {
            var clock = window.setInterval(function () {
                time=time-1000;
                //app.timeString=time/1000;//秒数

                app.timeString =app.convertTimeString(time);
                if(time<=0){
                    window.clearInterval(clock);
                }
            },1000)
        },
        //提交订单
        submitOrder:function () {
            axios.get("/seckillOrder/submitOrder.shtml?id="+this.seckillId).then(
                function (response) {//result
                    if(response.data.success){
                        //创建订单成功
                    }else{
                        if(response.data.message=='403'){
                            //没有登录，跳转到登录页面 再跳回来
                            var url = window.location.href;
                            window.location.href="http://localhost:9111/page/login.shtml?url="+url;
                        }else{
                            //alert(response.data.message);
                            app.messageInfo=response.data.message;
                        }
                    }
                }
            )
        },
        //点击立即抢购的时候调用
        queryOrderStatus:function () {
            var count=0;

            var queryfun=window.setInterval(function () {
                count+=3;
                if(count>100){
                    window.clearInterval(queryfun);
                }
                axios.get('/seckillOrder/queryOrderStatus.shtml').then(
                    function (response) {//result
                        if(response.data.success){
                            //订单创建成功 跳转到支付的页面
                            alert("订单创建成功");
                            window.location.href="pay/pay.html";
                        }else{
                            app.messageInfo=response.data.message+"....."+count;
                        }
                    }
                )
            },3000);


        },

        getGoodsById:function (id) {
            axios.get('/seckillGoods/getGoodsById.shtml?id='+id).then(
                function (response) {//获取到了map
                    app.caculate(response.data.time);
                    app.goodsInfo.count=response.data.count;
                }
            )
        }
    },
    created:function () {
        //页面加载的时候 获取URL中的参数的ID 就是商品的ID
        let obj = this.getUrlParam();
        //页面加载 的时候倒计时
        this.getGoodsById(obj.id);
        this.seckillId= obj.id;
    }
});