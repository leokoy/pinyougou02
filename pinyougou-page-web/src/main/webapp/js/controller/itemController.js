var app = new Vue({
	el:"#app",
	data:{
		num:1,
		//{"网络":"移动3G","机身内存":"16G"}
		//获取第一个默认的SKU的规格的数据赋值给他
		
		specificationItems:JSON.parse(JSON.stringify(skuList[0].spec)),//定义一个变量  用来存储当前规格（包括点击到的规格的选项值）的数据
		sku:skuList[0],//绑定SKU对象
		
	},
	methods:{
		/**
		 * 
		 * @param {Object} data   就是 1  或者 -1
		 */
		add:function(data){
			data = parseInt(data);
			this.num=this.num+data;
			if(this.num<=1){
				this.num=1;
			}
		},
		//就是点击规格的选项的时候 调用影响变量specificationItems的值
		selectSpecifcation:function(specName,specValue){
			
			this.$set(this.specificationItems,specName,specValue);
			
			this.search();
			
		},
		//判断 点击到的规格的选项是否在当前的规格的变量中存在，如果存在 return true,否则false
		isSelected:function(specName,specValue){
			if(this.specificationItems[specName]==specValue){
				return true;
			}
			return false;
		},
		//目的：就是循环遍历 SKU的列表数据  判断 点击到的规格的数据 是否在sku列表中存在，如果存在  直接影响变量SKU
		search:function(){
			for(var i=0;i<skuList.length;i++){
				//{"id":10561634,"title":"58iphonex 移动3G 16G","price":0.01,spec:{"网络":"移动3G","机身内存":"16G"}}
				var obj = skuList[i];//
				
				if(JSON.stringify(this.specificationItems)==JSON.stringify(obj.spec)){
					this.sku=obj;
					break;
				}
			}
		},
		//添加购物车
        addGoodsToCartList:function () {
			 axios.get('http://localhost:9107/cart/addGoodsToCartList.shtml',{
			 	params:{
			 		itemId:this.sku.id,
					num:this.num
				},
				 //客户端在AJax的时候携带cookie到服务器。
				withCredentials:true
			 }).then(
			 	function (response) {//result
					if(response.data.success){
						window.location.href="http://localhost:9107/cart.html";
					}else{
						alert("失败");
					}
                }
			 )
        }
	},
	created:function(){
		
	}
})
