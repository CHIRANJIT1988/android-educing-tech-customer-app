package educing.tech.customer.model;

import java.io.Serializable;
import java.util.List;

public class ChatMessage implements Serializable
{

	public String message_id, user_id, message, image, path, user_name, timestamp;
	public int read_status, sync_status, message_type, unread_message;


	public ChatMessage()
	{
		super();
	}


	public ChatMessage(String user_id, String user_name)
	{

		super();

		this.user_id = user_id;
		this.user_name = user_name;
	}

	public ChatMessage(String user_id, String user_name, String timestamp)
	{

		super();

		this.user_id = user_id;
		this.user_name = user_name;
		this.timestamp = timestamp;
	}


	public ChatMessage(String user_id, String user_name, String message, String timestamp, int unread_message)
	{

		super();

		this.user_id = user_id;
		this.user_name = user_name;
		this.message = message;
		this.timestamp = timestamp;
		this.unread_message = unread_message;
	}


	public ChatMessage(String message_id, String user_id, String message, String image, String timestamp, int read_status, int message_type)
	{

		super();

		this.message_id = message_id;
		this.user_id = user_id;
		this.message = message;
		this.image = image;
		this.timestamp = timestamp;
		this.read_status = read_status;
		this.message_type = message_type;
	}


	public void setUserName(String user_name)
	{
		this.user_name = user_name;
	}

	public String getUserName()
	{
		return this.user_name;
	}


	public void setTimestamp(String timestamp)
	{
		this.timestamp = timestamp;
	}

	public String getTimestamp()
	{
		return this.timestamp;
	}


	public void setMessageId(String message_id)
	{
		this.message_id = message_id;
	}

	public String getMessageId()
	{
		return this.message_id;
	}


	public void setUserId(String user_id)
	{
		this.user_id = user_id;
	}

	public String getUserId()
	{
		return this.user_id;
	}


	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return this.message;
	}


	public void setImage(String image)
	{
		this.image = image;
	}

	public String getImage()
	{
		return this.image;
	}


	public void setFilePath(String path)
	{
		this.path = path;
	}

	public String getFilePath()
	{
		return this.path;
	}


	public void setSyncStatus(int sync_status)
	{
		this.sync_status = sync_status;
	}

	/*public int getSyncStatus()
	{
		return this.sync_status;
	}*/


	public void setReadStatus(int read_status)
	{
		this.read_status = read_status;
	}

	public int getReadStatus()
	{
		return this.read_status;
	}


	public void setMessageType(int message_type)
	{
		this.message_type = message_type;
	}

	public int getMessageType()
	{
		return this.message_type;
	}


	public void setUnreadMessageCount(int unread_message)
	{
		this.unread_message = unread_message;
	}

	public int getUnreadMessageCount()
	{
		return this.unread_message;
	}


	public static int findUser(List<ChatMessage> userList, String user_id)
	{

		for(int index=0; index< userList.size(); index++)
		{

			if(userList.get(index).getUserId().equals(user_id))
			{
				return index;
			}
		}

		return -1;
	}


	public static String generateOrderChatMessage(List<Order> orderList, ShippingAddress shipping_address)
	{

		StringBuilder sAddress = new StringBuilder().append(shipping_address.getAddress().toUpperCase()).append(", ")
				.append(shipping_address.getLandmark().toUpperCase()).append(", ").append(shipping_address.getCity().toUpperCase())
				.append(", ").append(shipping_address.getState().toUpperCase()).append(", ").append(shipping_address.getPincode());

		String message = "ORDER LIST - \n";

		for(int i=0; i<orderList.size(); i++)
		{
			Order order = orderList.get(i);
			message += "\n" + (i + 1) + ". " + order.product_name + " " + order.weight + order.unit + " (Qty-" + order.quantity + ")";
		}

		message += "\n\nDELIVERY DETAILS - \n\n";
		message += shipping_address.getName().toUpperCase() + "\n";
		message += sAddress + "\n";
		message += "M# " + shipping_address.getPhoneNo();

		return message;
	}


	public static int findIndex(List<ChatMessage> messageList, String message_number)
	{

		for(int index=0; index< messageList.size(); index++)
		{

			if(messageList.get(index).getMessageId().equals(message_number))
			{
				return index;
			}
		}

		return -1;
	}
}