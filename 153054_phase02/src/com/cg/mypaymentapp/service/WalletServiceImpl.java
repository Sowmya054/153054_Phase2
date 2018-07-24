
package com.cg.mypaymentapp.service;

import java.sql.Statement;
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
import com.cg.mypaymentapp.exception.InsufficientBalanceException;
import com.cg.mypaymentapp.exception.InvalidInputException;
import com.cg.mypaymentapp.repo.WalletRepo;
import com.cg.mypaymentapp.repo.WalletRepoImpl;
import com.cg.mypaymentapp.util.DBUtil;

public class WalletServiceImpl implements WalletService {
	static Logger myLogger = Logger.getLogger(WalletServiceImpl.class);
	private WalletRepo repo;

	public WalletServiceImpl(WalletRepo repo) {
		super();
		myLogger.info("constructor called");

	}

	public WalletServiceImpl() {
		repo = new WalletRepoImpl();
	}

	public Customer createAccount(String name, String mobileNo, BigDecimal amount) {
		if (!isValidName(name) || !isValidMobile(mobileNo) || !isValidAmount(amount)) {
			throw new InvalidInputException("Sorry , your details are incorrect");
		}
		Customer cus = new Customer(name, mobileNo, new Wallet(amount));
		myLogger.info("create account");
		boolean b = repo.save(cus);
		return cus;

	}

	public Customer showBalance(String mobileNo) {
		if (!isValidMobile(mobileNo)) {
			throw new InvalidInputException("Invalid Mobile number");
		} else {
			Customer customer = repo.findOne(mobileNo);
			myLogger.info("show balance");
			if (customer != null)
				return customer;
			else
				throw new InvalidInputException("account with mobile number not found ");
		}
	}

	public Customer fundTransfer(String sourceMobileNo, String targetMobileNo, BigDecimal amount) {
		if (!isValidMobile(sourceMobileNo) || !isValidMobile(targetMobileNo) || !isValidAmount(amount)) {
			throw new InvalidInputException("Sorry , your details are incorrect");
		}
		Customer sourceCustomer = repo.findOne(sourceMobileNo);
		Customer destCustomer = repo.findOne(targetMobileNo);

		if (sourceCustomer != null && destCustomer != null) {
			Wallet balance1 = sourceCustomer.getWallet();
			Wallet balance2 = destCustomer.getWallet();
			if (balance1.getBalance().compareTo(amount) > 0) {
				BigDecimal remainBalance = balance1.getBalance().subtract(amount);
				BigDecimal addedBalance = balance2.getBalance().add(amount);
				balance1.setBalance(remainBalance);

				BigDecimal total = sourceCustomer.getWallet().getBalance();

				try (Connection con = DBUtil.getConnection()) {
					Statement stmt = con.createStatement();
					String str = "update customer set balance=" + total + " where mobileno=" + sourceMobileNo;

					stmt.executeUpdate(str);

				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try (Connection con = DBUtil.getConnection()) {
					Statement stmt = con.createStatement();
					String str = "update customer set balance=" + addedBalance + " where mobileno=" + targetMobileNo;

					stmt.executeUpdate(str);

				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				myLogger.info("fund transfer");
				return sourceCustomer;
			} else {
				throw new InsufficientBalanceException("insufficient balance");

			}
		} else {
			throw new InvalidInputException("account with mobile number not found ");
		}

	}

	public Customer depositAmount(String mobileNo, BigDecimal amount) {
		if (!isValidMobile(mobileNo) || !isValidAmount(amount)) {
			throw new InvalidInputException("Sorry , your details are incorrect");
		}
		Customer customer = repo.findOne(mobileNo);
		if (customer != null) {
			myLogger.info("deposit money");
			Wallet mywallet = new Wallet();
			mywallet = customer.getWallet();
			mywallet.setBalance(mywallet.getBalance().add(amount));
			BigDecimal total = customer.getWallet().getBalance();

			try (Connection con = DBUtil.getConnection()) {
				Statement stmt = con.createStatement();
				String str = "update customer set balance=" + total + " where mobileno=" + mobileNo;

				stmt.executeUpdate(str);

			} catch (ClassNotFoundException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return customer;
		} else {
			throw new InvalidInputException("account with mobile number not found ");
		}
	}

	public Customer withdrawAmount(String mobileNo, BigDecimal amount) {
		if (!isValidMobile(mobileNo) || !isValidAmount(amount)) {
			throw new InvalidInputException("Sorry , your details are incorrect");
		}
		Customer customer = repo.findOne(mobileNo);
		if (customer != null) {
			Wallet balance = customer.getWallet();
			if (balance.getBalance().compareTo(amount) > 0) {
				BigDecimal withdrawedBalance = balance.getBalance().subtract(amount);
				balance.setBalance(withdrawedBalance);
				BigDecimal total = customer.getWallet().getBalance();

				try (Connection con = DBUtil.getConnection()) {
					Statement stmt = con.createStatement();
					String str = "update customer set balance=" + total + " where mobileno=" + mobileNo;

					stmt.executeUpdate(str);

				} catch (ClassNotFoundException | SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				myLogger.info("withdraw money");
				return customer;
			} else {
				throw new InsufficientBalanceException("Insufficient balance ");
			}
		} else {
			throw new InvalidInputException("account with mobile number not found ");
		}
	}

	private boolean isValidMobile(String mobileNo) {
		if (String.valueOf(mobileNo).matches("[1-9][0-9]{9}")) {
			return true;
		} else {
			return false;
		}

	}

	private boolean isValidAmount(BigDecimal amount) {
		BigDecimal value = new BigDecimal("0");
		if (amount.compareTo(value) > 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isValidName(String name) {
		if (name.isEmpty()) {
			return false;
		} else {
			return true;
		}

	}

}
