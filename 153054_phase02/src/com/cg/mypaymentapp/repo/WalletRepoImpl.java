package com.cg.mypaymentapp.repo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cg.mypaymentapp.beans.Customer;
import com.cg.mypaymentapp.beans.Wallet;
import com.cg.mypaymentapp.service.WalletServiceImpl;
import com.cg.mypaymentapp.util.DBUtil;

public class WalletRepoImpl implements WalletRepo {

	static Logger myLogger = Logger.getLogger(WalletRepoImpl.class);

	public boolean save(Customer customer) {
		try (Connection con = DBUtil.getConnection()) {

			PreparedStatement pstm1 = con.prepareStatement("insert into customer values(?,?,?)");

			pstm1.setString(1, customer.getName());
			pstm1.setString(2, customer.getMobileNo());
			pstm1.setBigDecimal(3, customer.getWallet().getBalance());

			pstm1.execute();

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return true;
	}

	public Customer findOne(String mobileNo) {
		Customer cus = null;
		try (Connection con = DBUtil.getConnection()) {

			PreparedStatement pstm = con.prepareStatement("SELECT * FROM customer WHERE customer.mobileno = ?");

			pstm.setString(1, mobileNo);

			ResultSet rs = pstm.executeQuery();

			if (rs.next() != false) {
				cus = new Customer();
				cus.setName(rs.getString(1));
				cus.setMobileNo(rs.getString(2));
				Wallet wallet = new Wallet(rs.getBigDecimal(3));
				cus.setWallet(wallet);

			} else {
				return null;
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cus;

	}
}
