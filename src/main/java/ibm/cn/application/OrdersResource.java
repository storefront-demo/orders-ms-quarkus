package ibm.cn.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import ibm.cn.application.model.Cart;
import ibm.cn.application.model.Order;
import ibm.cn.application.repository.OrderRepository;

@Path("/micro/orders")
public class OrdersResource {
	
	@Inject
    JsonWebToken jwt;
	
	private static final Logger LOG = Logger.getLogger(OrdersResource.class);
	
	private final OrderRepository orderRepository;

	public OrdersResource(OrderRepository orderRepository) {
	  this.orderRepository = orderRepository;
	}
    
    @GET
//    @RolesAllowed({"user","admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrders() throws Exception {
        try {
            if (jwt == null) {
                // distinguishing lack of jwt from a poorly generated one
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing JWT").build();
            }
            else {
            	LOG.info("MP JWT config message: " + jwt.getName() );
            	LOG.info("MP JWT getIssuedAtTime " + jwt.getIssuedAtTime() );
            }
            final String customerId = jwt.getName();
            if (customerId == null) {
                // if no user passed in, this is a bad request
                // return "Invalid Bearer Token: Missing customer ID";
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Bearer Token: Missing customer ID from jwt: " + jwt.getRawToken()).build();
            }

            LOG.info("caller: " + customerId);

            final List<Order> orders = orderRepository.findByCustomerIdOrderByDateDesc(customerId);

            return Response.ok(orders).build();

        } catch (Exception e) {
            System.err.println(e.getMessage() + "" + e);
            System.err.println("Entering the Fallback Method from getOrders().");
            throw new Exception(e.toString());
        }

    }
    
    @GET
//    @RolesAllowed({"user","admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getOrdersById(@PathParam("id") String id) throws Exception {
	    try {
	    	if (jwt == null) {
	    		// distinguishing lack of jwt from a poorly generated one
	    		return Response.status(Response.Status.BAD_REQUEST).entity("Missing JWT").build();
	    	}
	    	else {
	    		LOG.info("MP JWT config message: " + jwt.getName() );
	    		LOG.info("MP JWT getIssuedAtTime " + jwt.getIssuedAtTime() );
	    	}
	          
	    	final String customerId = jwt.getName();
	          
	    	if (customerId == null) {
	              // if no user passed in, this is a bad request
	              // return "Invalid Bearer Token: Missing customer ID";
	              return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Bearer Token: Missing customer ID from jwt: " + jwt.getRawToken()).build();
	        }
	
	    	LOG.info("caller: " + customerId);
	    	final List<Order> orders = orderRepository.findByOrderId(id);
	          
	    	return Response.ok(orders).build();
	      
	    } 
	    catch (Exception e) { 
	    	System.err.println(e.getMessage() + "" + e);
	    	throw new Exception(e.toString());
	    }

    }

    @POST
//    @RolesAllowed({"user","admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(
        Order payload, @Context UriInfo uriInfo) throws IOException, TimeoutException {
        try {
            if (jwt == null) {
                // distinguishing lack of jwt from a poorly generated one
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing JWT").build();
            }
            final String customerId = jwt.getName();
            if (customerId == null) {
                // if no user passed in, this is a bad request
                //return "Invalid Bearer Token: Missing customer ID";
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Bearer Token: Missing customer ID from jwt: " + jwt.getRawToken()).build();
            }

            payload.setDate(Calendar.getInstance().getTime());
            payload.setCustomerId(customerId);

            String id = UUID.randomUUID().toString();

            payload.setId(id);

            LOG.info("New order: " + payload.toString());

            orderRepository.putOrderDetails(payload);

            UriBuilder builder = uriInfo.getAbsolutePathBuilder();
            builder.path(payload.getId());
           
            LOG.info(builder.build().toString());
            
            return Response.created(builder.build()).entity(payload).build();

        } catch (Exception ex) {
            System.err.println("Error creating order: " + ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating order: " + ex.toString()).build();
        }

    }
    
    @GET
//    @RolesAllowed({"user","admin"})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/cartprice")
    public Response getTotalPrice() throws Exception {
        try {
            if (jwt == null) {
                // distinguishing lack of jwt from a poorly generated one
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing JWT").build();
            }
            else {
            	LOG.info("MP JWT config message: " + jwt.getName() );
            	LOG.info("MP JWT getIssuedAtTime " + jwt.getIssuedAtTime() );
            }
            final String customerId = jwt.getName();
            if (customerId == null) {
                // if no user passed in, this is a bad request
                // return "Invalid Bearer Token: Missing customer ID";
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Bearer Token: Missing customer ID from jwt: " + jwt.getRawToken()).build();
            }

            LOG.info("caller: " + customerId);

            final List<Order> orders = orderRepository.findByCustomerIdOrderByDateDesc(customerId);
            
            int totalPrice = 0;
            for (Order o: orders) {
            	int price = o.getCount()*o.getPrice();
            	totalPrice += price;
            }
            
            Cart cart = new Cart(totalPrice);
            
//            List<Cart> priceData = new ArrayList<Cart>();
//            priceData.add(cart);
            
            return Response.ok(cart).build();

        } catch (Exception e) {
            System.err.println(e.getMessage() + "" + e);
            System.err.println("Entering the Fallback Method from getTotalPrice().");
            throw new Exception(e.toString());
        }

    }
    
}