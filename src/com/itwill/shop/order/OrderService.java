package com.itwill.shop.order;

import java.util.ArrayList;
import java.util.List;

import com.itwill.shop.cart.Cart;
import com.itwill.shop.cart.CartDao;
import com.itwill.shop.member.Member;
import com.itwill.shop.member.MemberDao;
import com.itwill.shop.product.Product;
import com.itwill.shop.product.ProductDao;

public class OrderService {
	
	private OrderDao orderDao;
	private ProductDao productDao;
	private CartDao cartDao;
	private MemberDao memberDao;
	
	public OrderService() throws Exception {
		orderDao = new OrderDao();
		productDao = new ProductDao();
		cartDao = new CartDao();
		memberDao = new MemberDao();
	}
	
	/*
	 * 주문 1개 삭제 - deleteByOrderNo
	 */
	public int deleteByOrderNo(int o_no) throws Exception {
		return orderDao.deleteByOrderNo(o_no);
	}
	
	/*
	 * 주문 전체 삭제 - delete
	 */
	public int delete(String m_id) throws Exception {
		return orderDao.deleteByMemberId(m_id);
	}
	
	/*
	 * 주문 목록 보기 - orderList
	 */
	public List<Order> orderList(String m_id) throws Exception {
		return orderDao.findByMemberId(m_id);
	}
	
	/*
	 * 주문상세보기 - orderDetail
	 */
	public Order orderDetail(String m_id, int o_no) throws Exception {
		return orderDao.findByOrderNo(m_id, o_no);
	}
	
	/*
	 * 상품에서 직접 주문 - orderCreate
	 */
	public int orderCreate(Order order, int p_no, int oi_qty) throws Exception {
		
		Member member = memberDao.findByPrimaryKey(order.getM_id());
		Product product = productDao.findByPrimaryKey(p_no);
		OrderItem orderItem = new OrderItem(0, oi_qty, product, 0);
		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
		String o_desc = "";
		
		orderItemList.add(orderItem);
		if (oi_qty >= 2) {
			o_desc = orderItemList.get(0).getProduct().getP_name() + " 외 " + (oi_qty - 1) + "개";
		} else if (oi_qty == 1) {
			o_desc = orderItemList.get(0).getProduct().getP_name();
		}
		Order newOrder = new Order(0,
								   o_desc,
								   null,
								   oi_qty * orderItemList.get(0).getProduct().getP_price(),
								   member.getM_address(), // 회원가입시 작성되는 주소
								   //order.getO_address(), 주문시 작성되는 주소 존재
								   order.getO_loc(),
								   order.getO_payment(),
								   order.getM_id());
		newOrder.setOrderItemList(orderItemList);
		return orderDao.insert(newOrder);
	}
	
	/*
	 * 카트에서 주문 - orderCreate 오버로딩
	 */
	public int orderCreate(Order order) throws Exception {
		
		Member member = memberDao.findByPrimaryKey(order.getM_id());
		List<Cart> cartList = cartDao.findByMemberId(order.getM_id());
		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
		int o_tot_price = 0;
		int oi_total_count = 0;
		String o_desc = "";
		int rowCount = 0;
		
		for (Cart cart : cartList) {
			OrderItem orderItem = new OrderItem(0, cart.getCart_qty(), cart.getProduct(), 0);
			orderItemList.add(orderItem);
			o_tot_price += orderItem.getOi_qty()*orderItem.getProduct().getP_price();
			oi_total_count += orderItem.getOi_qty();
		}
		if (orderItemList.size() >= 2) {
			o_desc = orderItemList.get(0).getProduct().getP_name() + " 외 " + (oi_total_count - 1) + "개";
		} else if (orderItemList.size() == 1) {
			o_desc = orderItemList.get(0).getProduct().getP_name();
		}
		//order.getO_address() 주문시 작성되는 주소 존재
		Order newOrder = new Order(0, o_desc, null, o_tot_price, member.getM_address(), order.getO_loc(), order.getO_payment(), order.getM_id());
		newOrder.setOrderItemList(orderItemList);
		rowCount = orderDao.insert(newOrder);
		cartDao.deleteByMemberId(order.getM_id());
		return rowCount;
	}
	
	/*
	 * 카트에서 선택 주문 - orderCreate 오버로딩 (선택)
	 */
	public int orderCreate(Order order, String[] cart_no_StrArray) throws Exception {
		
		Member member = memberDao.findByPrimaryKey(order.getM_id());
		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
		int o_tot_price = 0;
		int oi_total_count = 0;
		String o_desc = "";
		int rowCount = 0;
		
		for (int i = 0; i < cart_no_StrArray.length; i++) {
			Cart cart = cartDao.findByCartNo(Integer.parseInt(cart_no_StrArray[i]));
			OrderItem orderItem = new OrderItem(0, cart.getCart_qty(), cart.getProduct(), 0);
			orderItemList.add(orderItem);
			o_tot_price += orderItem.getOi_qty() * orderItem.getProduct().getP_price();
			oi_total_count += orderItem.getOi_qty();
		}
		if (orderItemList.size() >= 2) {
			o_desc = orderItemList.get(0).getProduct().getP_name() + " 외 " + (oi_total_count - 1) + "개";
		} else if (orderItemList.size() == 1) {
			o_desc = orderItemList.get(0).getProduct().getP_name();
		}
		//order.getO_address() 주문시 작성되는 주소 존재
		Order newOrder = new Order(0, o_desc, null, o_tot_price, member.getM_address(), order.getO_loc(), order.getO_payment(), order.getM_id());
		newOrder.setOrderItemList(orderItemList);
		rowCount = orderDao.insert(newOrder);
		for(int i=0; i<cart_no_StrArray.length; i++) {
			cartDao.deleteByCartNo(Integer.parseInt(cart_no_StrArray[i]));
		}
		return rowCount;
	}
}