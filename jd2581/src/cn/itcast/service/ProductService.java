package cn.itcast.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.itcast.pojo.ProductModel;
import cn.itcast.pojo.QueryVo;
import cn.itcast.pojo.ResultModel;
@Service
public class ProductService {
	
	private static Integer PAGE_SIZE=60;
	@Autowired
	SolrServer solrServer;
	/**
	 * 返回值分析
	 * 1.list<productModel>
	 * 2.当前页
	 * 3.总页数
	 * 4.一共多少符合条件的商品数量
	 * @throws SolrServerException 
	 */
   public ResultModel jdsearch(QueryVo vo) throws SolrServerException{
	   ResultModel resultModel=new ResultModel();
	   //设置显示的当前页
	   if(vo.getPage()==null){
		   vo.setPage(1);
	   }
	   resultModel.setCurPage(vo.getPage());
	   //创建查询语句
	   SolrQuery query=new SolrQuery();
	   //设置搜索的关键词
	   if(vo.getQueryString()!=null && !vo.getQueryString().equals("")){
		   query.setQuery(vo.getQueryString());  
	   }else{
		   query.setQuery("*:*");//如果关键词为空,返回所有商品
	   }
	   //设置默认查询域名
	   query.set("df", "product_keywords");
	   
	   //商品类别筛选
	   if(vo.getCatalog_name()!=null && !vo.getCatalog_name().equals("")){
		   query.addFilterQuery("product_catalog_name:"+vo.getCatalog_name());  
	   }
	   
	   //价格筛选 0-9
	   if(vo.getPrice()!= null && !vo.getPrice().equals("")){
		   String[] split = vo.getPrice().split("-");
		   query.addFilterQuery("product_price:["+split[0]+" TO "+split[1]+"]");
	   }
	   //根据价格排序
	   if(vo.getSort() != null){
		   query.setSort("product_price",vo.getSort()==0 ? ORDER.asc:ORDER.desc);
	   }
	   
	   //分页设置
	   query.setStart((vo.getPage()-1)*PAGE_SIZE);
	   query.setRows(PAGE_SIZE);//设置一页显示多少条
	   
	    //solr中高亮功能 默认是关闭的状态,我们需手动开启
	   query.setHighlight(true);//开启高亮服务
	   query.addHighlightField("product_name");//添加高亮显示的域名
	    //设置高亮前缀
	   query.setHighlightSimplePre("<font style=\"color:red;\">");
	    //设置高亮后缀
	   query.setHighlightSimplePost("</font>");
	   
	   //执行查询语句
	   QueryResponse response = solrServer.query(query);
	   
	   //获取普通结果集
	   SolrDocumentList results = response.getResults();
	   
	   //一共获取符合条件的总条数
	   long numFound = results.getNumFound();
	   resultModel.setRecordCount(numFound);
	   
	   //计算总页数
	   Long allpageCount=numFound%PAGE_SIZE>0 ? numFound/PAGE_SIZE+1 : numFound/PAGE_SIZE;
	   resultModel.setPageCount(allpageCount);
	   
	   //获取高亮结果集
	   Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
	   List<ProductModel> productModels=new ArrayList<>();
	   for (SolrDocument document : results) {
		   ProductModel productModel=new ProductModel();
		   //获取高亮的商品名称
		   List<String> list = highlighting.get(document.get("id")).get("product_name");
		   if(list != null && list.size()>0){
			   productModel.setName(list.get(0));
		   }else{//如果没有高亮的名称显示,我们就显示成普通的名称
			   productModel.setName(String.valueOf(document.get("product_name")));
		   }
		   productModel.setPid(String.valueOf(document.get("id")));
		   productModel.setCatalog_name(String.valueOf(document.get("product_catalog_name")));
		   productModel.setPicture(String.valueOf(document.get("product_picture")));
		   productModel.setPrice(Float.valueOf(String.valueOf(document.get("product_price"))));
		   productModels.add(productModel);
	   }
	   
	   //设置商品显示的结果集
	   resultModel.setProductList(productModels);
	   return resultModel;
	   
   }
}
