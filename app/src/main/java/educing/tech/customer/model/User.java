package educing.tech.customer.model;


import java.io.Serializable;


public class User implements Serializable
{

	public int user_id;
	public String name, phoneNo, email, password, confirmation_code, deviceId;


	public User()
	{
		
	}


	public User(int user_id, String name, String phoneNo)
	{
		this.user_id = user_id;
		this.name = name;
		this.phoneNo = phoneNo;
	}


	public void setUserID(int user_id)
	{
		this.user_id = user_id;
	}

	public int getUserID()
	{
		return this.user_id;
	}


	public void setUserName(String name)
	{
		this.name = name;
	}
	
	public String getUserName()
	{
		return this.name;
	}


	public void setPhoneNo(String phoneNo)
	{
		this.phoneNo = phoneNo;
	}

	public String getPhoneNo()
	{
		return this.phoneNo;
	}


	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getEmail()
	{
		return this.email;
	}
	
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getPassword()
	{
		return this.password;
	}


	public void setDeviceId(String deviceId)
	{
		this.deviceId = deviceId;
	}

	public String getDeviceId()
	{
		return this.deviceId;
	}


	public void setConfirmationCode(String confirmation_code)
	{
		this.confirmation_code = confirmation_code;
	}

	public String getConfirmationCode()
	{
		return this.confirmation_code;
	}
}
