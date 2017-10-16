package cn.itcast.action;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.itcast.pojo.QueryVo;
import cn.itcast.pojo.ResultModel;
import cn.itcast.service.ProductService;

@Controller
@RequestMapping("/product")
public class ProductAction {
    @Autowired
	ProductService productService;
	
	@RequestMapping("/list")
	public String list(QueryVo queryVo,Model model) throws SolrServerException{
		ResultModel resultModel = productService.jdsearch(queryVo);
		model.addAttribute("result", resultModel);
		
		//设置前台条件的回显
		model.addAttribute("vo",queryVo);
		return "product_list";
		
	}
}
