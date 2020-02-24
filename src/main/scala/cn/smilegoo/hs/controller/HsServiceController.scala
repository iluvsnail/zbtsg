package cn.smilegoo.hs.controller

import com.alibaba.fastjson.{JSON, JSONObject}
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation._
import org.springframework.web.client.RestTemplate

import scala.collection.JavaConversions._



@Api(value = " micro services api",tags=Array("ZBTSG tool services api"))
@RestController
@RequestMapping(Array("/zbtsg/api/v1/"))
class HsServiceController {

  @Autowired
  val restTemplate:RestTemplate=null

  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(name = "id", value = "id", required = true,dataType = "string",  paramType = "query",defaultValue = "输入文章id")
    )
  )
  @ApiOperation(value = "获取提取注释后文档内容",
    notes = "")
  @RequestMapping(value = Array("/translate"),method = Array(RequestMethod.POST,RequestMethod.GET),produces = Array(MediaType.TEXT_HTML_VALUE))
  def translatePage(@RequestParam  id:String):String ={
    var rst = ""
    val doc=Jsoup.connect("https://bsalib.com/api/v1/textpage?id="+id).ignoreContentType(true).get();
    doc.select("span").foreach(e=>{
      val zhushi = e.attr("title").replaceAll("\\\\\"","")
      e.append("<span class='sml'>("+zhushi+")</span>")
      e.attr("title","")
    })
    val d = doc.html()
    rst=d
    //rst = d.substring(d.indexOf("html")+8)
    rst=rst.replaceAll("\\\\n","")
    rst=rst.replaceAll("\\\\&quot;","")
    rst=rst.substring(0,rst.indexOf("{"))+rst.substring(rst.indexOf("html",2)+8)
    rst = rst.substring(0,rst.indexOf("audio_url")-4)+"</body></html>"
    //rst="<html><body>"+rst+"</body></html>"
    val dd = Jsoup.parse(rst)
    dd.head().append("<style>.songci{text-indent: 0;   text-align: center;   font-weight: 700;    font-family: kaiti,kai;    font-size: 1.4em;}" +
        ".title{max-width: 100%;    margin: 0 auto;    text-align: center;   font-size: 1.3em;    font-family: \"黑体\";    font-weight: 700;    line-height: 2.5em;}" +
        ".sml{font-size:14;color: gray;}" +
        ".zhushi{    margin: 0 2px;    color: #c74f4d;    border-bottom: 1px dotted #c74f4d;}</style>")
    dd.head().append("<title>"+dd.select(".title").text()+"</title>")
    rst=dd.html()
    rst
  }

}
