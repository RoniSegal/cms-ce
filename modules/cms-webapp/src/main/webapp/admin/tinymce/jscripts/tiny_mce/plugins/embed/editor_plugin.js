(function(){tinymce.PluginManager.requireLangPack("embed");var a=/^(mceItemIframe|mceItemFlash|mceItemShockWave|mceItemWindowsMedia|mceItemQuickTime|mceItemRealMedia)$/;tinymce.create("tinymce.plugins.embed",{init:function(c,d){var e=this;function b(f){return a.test(f.className)}e.url=d;e.editor=c;e.embed_iframe_innerhtml_fallback=(c.settings.embed_iframe_innerhtml_fallback)?c.settings.embed_iframe_innerhtml_fallback:"This browser does not support the iframe element.";c.onPreInit.add(function(){c.serializer.addRules("iframe[_iframe_innerhtml|align<bottom?left?middle?right?top|class|frameborder|height|id|longdesc|marginheight|marginwidth|name|scrolling<auto?no?yes|src|style|title|width|type]")});c.onInit.add(function(){if(c.settings.content_css!==false){c.dom.loadCSS(d+"/css/embed.css")}});c.addCommand("enonicEmbed",function(){c.windowManager.open({file:d+"/window.html",width:620+c.getLang("embed.delta_width",0),height:540+c.getLang("embed.delta_height",0),inline:1},{plugin_url:d})});c.addButton("embed",{title:"embed.desc",cmd:"enonicEmbed",image:d+"/img/embed.gif"});c.onNodeChange.add(function(g,f,h){f.setActive("embed",b(h))});c.onBeforeSetContent.add(function(f,g){g.content=e.iframesToSpans(g.content)});c.onSetContent.add(function(f,g){e.spansToImages(g.node)});c.onPreProcess.add(function(f,g){if(g.set){g.content=e.iframesToSpans(g.content);e.spansToImages(g.node)}if(g.get){e.imagesToIframes(g)}});c.onPostProcess.add(function(f,g){if(g.get){g.content=g.content.replace(/(<iframe.+?)_iframe_innerhtml="(.+?)"(.+?)(<\/iframe>)/gi,function(){var h=e.editor.dom.decode(arguments[2]).replace(/&lt;/gm,"<").replace(/&gt;/gm,">");return arguments[1]+tinymce.trim(arguments[3])+h+arguments[4]})}})},getInfo:function(){return{longname:"Embed Plug-in",author:"tan@enonic.com",authorurl:"http://www.enonic.com",infourl:"http://www.enonic.com",version:"1.1"}},iframesToSpans:function(c){var b=this;return c.replace(/<iframe\s*(.*?)>(|[\s\S]+?)<\/iframe>/gim,function(){var d="<span ";d+=arguments[1];d+=' _class="mceItemIframe">';d+=b.editor.dom.encode(arguments[2]);d+="</span>";return d})},spansToImages:function(e){var c=this,d=c.editor,f=d.dom,g;var b=f.select('span[_class="mceItemIframe"]',e);tinymce.each(b,function(h){g=c.createImagePlaceHolder(h);f.replace(g,h)})},createImagePlaceHolder:function(i){var k=this,g=k.editor,f=g.dom;var d,h,b,j,c,e;b=f.getAttrib(i,"width");j=f.getAttrib(i,"height");h=k._serializeIframeAttributes(i);e=i.innerHTML.replace(/^\s+|\s+$/g,"");e=e!==""?e:k.embed_iframe_innerhtml_fallback;e=e.replace(/<(.+?)>/gim,function(){return"<"+arguments[1].toLowerCase()+">"});h+=',"innerhtml":"'+e+'"';d=f.create("img");f.setAttrib(d,"src",k.url+"/img/trans.gif");f.setAttrib(d,"title",h);f.setAttrib(d,"width",b);f.setAttrib(d,"height",j);f.addClass(d,"mceItemIframe");f.setStyle(d,"width",b);f.setAttrib(d,"style",0);return d},imagesToIframes:function(g){var c=this,e=c.editor,f=e.dom,d;var b=f.select('img[class="mceItemIframe"]',g.node);tinymce.each(b,function(h){d=c.createIframeElement(h);f.replace(d,h)})},createIframeElement:function(c){var j=this,g=j.editor,d=g.dom;var f=j._parseImagePlaceHolderTitle(c);var h="",e,b,i;if("innerhtml" in f){h=f.innerhtml;delete f.innerhtml}b=d.getAttrib(c,"width");i=d.getAttrib(c,"height");e=d.create("iframe",f);d.setAttrib(e,"width",b);d.setAttrib(e,"height",i);d.setAttrib(e,"_iframe_innerhtml",h);return e},_parseImagePlaceHolderTitle:function(e){var b=this,c=b.editor;var d=c.dom.getAttrib(e,"title");return tinymce.util.JSON.parse("{"+d+"}")},_serializeIframeAttributes:function(d){var j=this,h=j.editor,c=h.dom,g,f,k;var b=["src","width","height","longdesc","name","frameborder","marginwidth","marginheight","scrolling","align","id","class","style","title","type"];var e={};for(var i in b){f=b[i];k=c.getAttrib(d,f);g=k!=="";if(g){e[f]=k}}return tinymce.util.JSON.serialize(e).replace(/[{}]/g,"")}});tinymce.PluginManager.add("embed",tinymce.plugins.embed)})();