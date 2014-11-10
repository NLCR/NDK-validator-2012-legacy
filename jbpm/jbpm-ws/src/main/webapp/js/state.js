var jbpmLinksFlagName="jbpmConsoleLinksFlag"; 

function setCookie(c_name,value,exdays){
  console.log("setting cookie name:"+c_name+" value:"+value+" exdays:"+exdays);
  var exdate=new Date();
  exdate.setDate(exdate.getDate() + exdays);
  var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
  document.cookie=c_name + "=" + c_value;
}

function getCookie(c_name){
  console.log("getting cookie:"+c_name);
  var c_value = document.cookie;
  var c_start = c_value.indexOf(" " + c_name + "=");
  if (c_start == -1){
    c_start = c_value.indexOf(c_name + "=");
  }
  
  if (c_start == -1){
    c_value = null;
  }
  else {
    c_start = c_value.indexOf("=", c_start) + 1;
    var c_end = c_value.indexOf(";", c_start);
    if (c_end == -1) {
      c_end = c_value.length; 
    }
    c_value = unescape(c_value.substring(c_start,c_end));
  }
  return c_value;
}


function onLoad(){
  if ( document.location.hash.indexOf("linksOn")>0 ){
  	setCookie(jbpmLinksFlagName,"true", 10000);
  }
  else if ( document.location.hash.indexOf("linksOff")>0 ){
  	setCookie(jbpmLinksFlagName,"false", 10000);
  }	
  
  flag=getCookie(jbpmLinksFlagName);
  if ( flag!=null){
    if ("true" == flag){
	  initKibanaLinks();
	}
  }
}

function initKibanaLinks(){
  $(".linkable-datetime").each(
    function (idx){
       var oldContent=$(this).text();
       oldContent=oldContent.replace(/</g,"&lt;"); //XSS protection
       var newContent="<a href='http://hpwrtst01/kibana-short/index.html#"
                      +encodeURIComponent(oldContent)
                      +"' target='kibana-detail'>"
                      +oldContent
                      +"</a>";
       $(this).html(newContent);
    }
  );  
}


$(document).ready(onLoad);

