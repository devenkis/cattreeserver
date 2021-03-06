package com.example.helloworld.resources;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.example.helloworld.core.Category;
import com.example.helloworld.core.UpdatePositionBean;
import com.example.helloworld.dao.DAOHandler;
import com.example.helloworld.dao.TreeDAO;



@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoriesResource {
	public final TreeDAO categoriesDAO;

	public CategoriesResource(TreeDAO treeDAO) {
		super();
		this.categoriesDAO = treeDAO;
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createCategory(Category category) {
		Response retResponse;
		int status = 200;
		Category returnValue = null;
		try {
			int count = categoriesDAO.checkCategoryBeforeInsertionIdempotency(category.getParentId(), category.getName());
			if(count == 0) {
				returnValue = category;
				int siblingCount = categoriesDAO.getSiblingCount(category.getParentId());
				category.setSiblingNumber(++siblingCount);
				returnValue.setId(categoriesDAO.createNode(category));
			} else {
				status = 400; //bad request, repeated request
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = 500;
		}
		retResponse = Response.status(status).entity(returnValue).build();
		return retResponse;
		//returns the category id the newly created category
	}
	
	@PUT
	@Path("position")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateCategoryPosition(UpdatePositionBean updatePositionBeanObject) {
		int status = 200;
		try {
			int updated = categoriesDAO.updatePosition(updatePositionBeanObject);
			System.out.println("updated position: "+updated);
			return getCategory(updatePositionBeanObject.getCategoryToUpdate().getParentId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = 500;
		}
		Response retResponse = Response.status(status).entity(null).build();
		return retResponse;
	}
	
	@PUT
	public Response updateCategory(Category category) {
		Response retResponse;
		int status = 200;
		boolean retValue = true;
		try {
			if(category.getId() == DAOHandler.CATEGORYTREE_ROOTID) {
				status = 406; //not acceptable
				retValue = false;
			} else if(categoriesDAO.updateNode(category)<=0) {
				status = 400; //bad request
				retValue = false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = 500;
			retValue = false;
		}
		retResponse = Response.status(status).entity(retValue).build();
		return retResponse;
	}
	
	@DELETE
	@Path("{id}")
	public Response deleteCategory(@PathParam("id") int id) {
		Response retResponse;
		int status = 200;
		boolean retValue= true;
		if(id == DAOHandler.CATEGORYTREE_ROOTID)
			{
			status = 406; //not acceptable
			retValue = false;
			retResponse = Response.status(status).entity(retValue).build();
			return retResponse;
			}
		try {
			if(categoriesDAO.deleteNode(id) <=0) {
				status = 400; //bad request
				retValue = false;
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = 500;
			retValue = false;
		}
		retResponse = Response.status(status).entity(retValue).build();
		return retResponse;
	}
	
	@GET
	@Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
	public Response getCategory(@PathParam("id") int id) {
		int status = 200;
		Category category = null;
		List<Category> categoryList;
		try {
			categoryList = categoriesDAO.getNode(id);
			category = categoryList.get(0);
			//Populate with its children
			category.setChildren(categoriesDAO.getChildren(category.getId()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = 500;
		}
		
		Response retResponse = Response.status(status).
                entity(category).
                /*header("Access-Control-Allow-Origin", "*").
                header("Access-Control-Allow-Credentials","true").*/ build();
		return retResponse;
	}
	
	@POST
	@Path("reset")
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean resetCategory() {
		boolean returnValue = false;
		try {
			returnValue = DAOHandler.getInstance().resetAndReload();
		} catch (Exception e) {
			e.printStackTrace();
			returnValue = false;
		}
		return returnValue;
	}
	
	//VTRAO: UNUSED API: TESTING PURPOSES
	@GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
	public Response getAllCategories() {
		List<Category> categoryList = null;
		int status = 200;
		try {
			categoryList = categoriesDAO.getNode(-1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = 500;
		}
		//return categoryList;
		
		Response retResponse = Response.status(status).
                entity(categoryList).
                /*header("Access-Control-Allow-Origin", "*").
                header("Access-Control-Allow-Credentials","true").*/ build();
		return retResponse;
	}
	
	@GET
	@Path("{id}/children")
    @Produces(MediaType.APPLICATION_JSON)
	public List<Category> getSubCategories(@PathParam("id") int id) {
		List<Category> categoryList = null;
		try {
			categoryList = categoriesDAO.getChildren(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return categoryList;
	}
	
	//TODO: ERROR INPUT Handling
	//TODO: All negative cases
	//TODO: Idempotent api(for create/post alone): Solution: Ensure you query for same displayname and parent if they match dont create again
	//TODO: data return should be based on generic response format
}
