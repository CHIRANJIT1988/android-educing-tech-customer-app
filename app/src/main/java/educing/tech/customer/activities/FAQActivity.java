package educing.tech.customer.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import educing.tech.customer.R;
import educing.tech.customer.adapter.ExpandableListAdapter;


public class FAQActivity extends AppCompatActivity {

	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);


		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		assert getSupportActionBar() != null;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle("FAQs");

		// get the listview
		expListView = (ExpandableListView) findViewById(R.id.lvExp);
		// preparing list data
		prepareListData();
		listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
		// setting list adapter
		expListView.setAdapter(listAdapter);

		for(int i=0; i<5; i++)
		{
			expListView.expandGroup(i);
		}

		// Listview Group click listener
		expListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
										int groupPosition, long id) {
				// Toast.makeText(getApplicationContext(),
				// "Group Clicked " + listDataHeader.get(groupPosition),
				// Toast.LENGTH_SHORT).show();
				return false;
			}
		});


		// Listview Group expanded listener
		expListView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				/*Toast.makeText(getApplicationContext(),
						listDataHeader.get(groupPosition) + " Expanded",
						Toast.LENGTH_SHORT).show();*/
			}
		});


		// Listview Group collasped listener
		expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				/*Toast.makeText(getApplicationContext(),
						listDataHeader.get(groupPosition) + " Collapsed",
						Toast.LENGTH_SHORT).show();*/

			}
		});


		// Listview on child click listener
		expListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				/*Toast.makeText(
						getApplicationContext(),
						listDataHeader.get(groupPosition)
								+ " : "
								+ listDataChild.get(
								listDataHeader.get(groupPosition)).get(
								childPosition), Toast.LENGTH_SHORT)
						.show();*/
				return false;
			}
		});
	}


	/*
	 * Preparing the list data
	 */
	private void prepareListData() {

		listDataHeader = new ArrayList<>();
		listDataChild = new HashMap<>();

		// Adding child data
		listDataHeader.add("What is txtBravo ?");
		listDataHeader.add("What are the advantages of using txtBravo ?");
		listDataHeader.add("Will i be charged extra by the businesses if i reach them through txtBravo ?");
		listDataHeader.add("How will i know about offers by the various businesses ?");
		listDataHeader.add("How long it will take for the business to respond to my chats ?");


		// Adding child data
		List<String> faq1 = new ArrayList<>();
		faq1.add("txtBravo is an app that lets users find and chat directly with various businesses in both the products and services industries.");

		List<String> faq2 = new ArrayList<>();
		faq2.add("txtBravo empowers you to search and deal directly with any businesses without any middle men. It also helps you to get great deals from businesses.");

		List<String> faq3 = new ArrayList<>();
		faq3.add("txtBravo only helps you to connect and deal with businesses. The transaction and dealing between you and the business in independent. txtBravo does not play any role in it.");

		List<String> faq4 = new ArrayList<>();
		faq4.add("You will receive notifications of various offers from the businesses. You can also find the existing offers in the deal section.");

		List<String> faq5 = new ArrayList<>();
		faq5.add("Most of the businesses respond quickly when  they are online. Response to the chat from business is at the  discretion of the business.");

		listDataChild.put(listDataHeader.get(0), faq1); // Header, Child data
		listDataChild.put(listDataHeader.get(1), faq2);
		listDataChild.put(listDataHeader.get(2), faq3);
		listDataChild.put(listDataHeader.get(3), faq4);
		listDataChild.put(listDataHeader.get(4), faq5);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		switch (item.getItemId())
		{

			case android.R.id.home:

				finish();
				break;
		}

		return super.onOptionsItemSelected(item);
	}
}