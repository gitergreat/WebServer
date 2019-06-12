/*
  public javascript 
  所有页面公用js - 导航/版权/回到顶部等
*/
$(".titlenav li").hover(function(){
   var i = $(this).index();
   $(this).addClass("active").siblings().removeClass("active");
   $(".tabdiv").hide();
   $(".tabdiv").eq(i).show();
})
$(".nav > li").hover(function(){ 
   if ( $(this).find(".nav-submenu").length>0 ){
	$(".nav-submenu").hide();
	$(this).find(".nav-submenu").show();
   }else{
	 $(".nav-submenu").hide();
   }
},function(){
   $(this).find(".nav-submenu").hide();
})
$(".nav-submenu").hover(function(e){
   e.stopPropagation();
})
$(".nav-submenu").mouseleave(function(){
   $(this).hide();
})
$(".go-search").click(function(){
   $(".search-block").fadeIn(); 
})	
$(".se-close").click(function(){
   $(".search-block").fadeOut(); 
})
//回到顶部
var backIcon = $("#backTop");
window.onscroll = function(){
    var osTop = document.documentElement.scrollTop || document.body.scrollTop;
    if (osTop >= 200) {
      backIcon.fadeIn();
    }else{
      backIcon.fadeOut();
    }
   } 
   backIcon.click(function(){
     $("html,body").animate({scrollTop:0}, 500);
})

//新增
$("#gztit,#gztit1").click(function(){
	$(".rwmimg").toggle();

});

