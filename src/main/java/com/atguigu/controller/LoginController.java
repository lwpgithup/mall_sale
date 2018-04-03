package com.atguigu.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.atguigu.bean.T_MALL_SHOPPINGCAR;
import com.atguigu.bean.T_MALL_USER_ACCOUNT;
import com.atguigu.mapper.LoginMapper;
import com.atguigu.service.CartServiceInf;
import com.atguigu.util.MyJsonUtil;

@Controller
public class LoginController {

	@Autowired
	LoginMapper loginMapper;
	@Autowired
	CartServiceInf cartServiceInf;

	@RequestMapping("login")
	public String goto_login(HttpServletResponse response, HttpSession session, T_MALL_USER_ACCOUNT user,
			HttpServletRequest request, @CookieValue(value = "list_cart_cookie",required=false) String list_cart_cookie,
			ModelMap map) {

		// 登陆，远程用户认证接口
		T_MALL_USER_ACCOUNT select_user = loginMapper.select_user(user);

		if (select_user == null) {
			return "redirect:/goto_login.do";
		} else {
			session.setAttribute("user", select_user);

			// 在客户端存储用户个性化信息，方便用户下次再访问网站时使用
			try {
				Cookie cookie = new Cookie("yh_mch", URLEncoder.encode(select_user.getYh_mch(), "utf-8"));
				// cookie.setPath("/");
				cookie.setMaxAge(60 * 60 * 24);
				response.addCookie(cookie);

				Cookie cookie2 = new Cookie("yh_nch", URLEncoder.encode("周润发", "utf-8"));
				// cookie.setPath("/");
				cookie2.setMaxAge(60 * 60 * 24);
				response.addCookie(cookie2);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			combine_cart(select_user, response, session, list_cart_cookie);
		}
	
		return "redirect:/index.do";
	}

	private void combine_cart(T_MALL_USER_ACCOUNT user, HttpServletResponse response, HttpSession session,
			String list_cart_cookie) {
		List<T_MALL_SHOPPINGCAR> list_cart = new ArrayList<T_MALL_SHOPPINGCAR>();
		// 判断cookie是否为空
		if (StringUtils.isEmpty(list_cart_cookie)){
			
		}else {
			List<T_MALL_SHOPPINGCAR> list_cart_db = cartServiceInf.get_list_cart_by_user(user);
			list_cart = MyJsonUtil.json_to_list(list_cart_cookie, T_MALL_SHOPPINGCAR.class);
			// 判断db是否为空
			if (list_cart_db == null || list_cart_db.size() == 0) {
				for (int i = 0; i < list_cart.size(); i++) {
					list_cart.get(i).setYh_id(user.getId());
					cartServiceInf.add_cart(list_cart.get(i));
				}
			} else {
				for (int i = 0; i < list_cart_db.size(); i++) {
					list_cart.get(i).setYh_id(user.getId());
					boolean b = if_new_cart(list_cart_db, list_cart.get(i));

					if (b) {
						for (int j = 0; j < list_cart_db.size(); j++) {
							// 判断sku_id是否存在
							if (list_cart.get(i).getSku_id() == list_cart_db.get(j).getSku_id()) {
								list_cart.get(i).setTjshl(list_cart.get(i).getTjshl() + list_cart_db.get(j).getTjshl());
								cartServiceInf.add_cart(list_cart.get(i));
							}
						}
						
					} else {
						cartServiceInf.add_cart(list_cart.get(i));
					}
				}
			}
		}
		// 同步session清空cookie
		session.setAttribute("list_cart_session", cartServiceInf.get_list_cart_by_user(user));
		response.addCookie(new Cookie("list_cart_cookie", ""));
	}
//	private void combine_cart(T_MALL_USER_ACCOUNT user, HttpServletResponse response, HttpSession session,
//			String list_cart_cookie) {
//		List<T_MALL_SHOPPINGCAR> list_cart = new ArrayList<T_MALL_SHOPPINGCAR>();
//		// 判断cookie是否为空
//		if (!StringUtils.isEmpty(list_cart_cookie)) {
//			
//			List<T_MALL_SHOPPINGCAR> list_cart_db = cartServiceInf.get_list_cart_by_user(user);
//			list_cart = MyJsonUtil.json_to_list(list_cart_cookie, T_MALL_SHOPPINGCAR.class);
//			// 判断db是否为空
//			if (list_cart_db == null || list_cart_db.size() == 0) {
//				for (int i = 0; i < list_cart.size(); i++) {
//					list_cart.get(i).setYh_id(user.getId());
//					cartServiceInf.add_cart(list_cart.get(i));
//				}
//			} else {
//				for (int i = 0; i < list_cart_db.size(); i++) {
//					boolean b = if_new_cart(list_cart_db, list_cart.get(i));
//					
//					if (b) {
//						list_cart.get(i).setYh_id(user.getId());
//						cartServiceInf.add_cart(list_cart.get(i));
//					} else {
//						for (int j = 0; j < list_cart_db.size(); j++) {
//							// 判断sku_id是否存在
//							if (list_cart.get(i).getSku_id() == list_cart_db.get(j).getSku_id()) {
//								list_cart.get(i).setTjshl(list_cart.get(i).getTjshl() + list_cart_db.get(j).getTjshl());
//								cartServiceInf.add_cart(list_cart.get(i));
//							}
//						}
//						
//					}
//				}
//			}
//		}
//		// 同步session清空cookie
//		session.setAttribute("list_cart_session", cartServiceInf.get_list_cart_by_user(user));
//		response.addCookie(new Cookie("list_cart_cookie", ""));
//	}

	private boolean if_new_cart(List<T_MALL_SHOPPINGCAR> list_cart, T_MALL_SHOPPINGCAR cart) {
		boolean b = true;
		for (int i = 0; i < list_cart.size(); i++) {
			if (list_cart.get(i).getSku_id() == cart.getSku_id()) {
				b = false;
			}
		}
		return b;
	}

}