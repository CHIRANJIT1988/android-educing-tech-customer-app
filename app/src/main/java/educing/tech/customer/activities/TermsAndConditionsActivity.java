package educing.tech.customer.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;

import educing.tech.customer.R;


public class TermsAndConditionsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.quickreturn_toolbar);
        setSupportActionBar(toolbar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Terms & Conditions");


        String content = "<html><body style='background:#fff'><p align='justify'>";

        content += "<b>INTRODUCTION</b><BR><BR>";
        content += getResources().getString(R.string.introduction_para_1) + "<BR><BR>";
        content += getResources().getString(R.string.introduction_para_2) + "<BR><BR>";
        content += getResources().getString(R.string.introduction_para_3) + "<BR><BR>";
        content += getResources().getString(R.string.introduction_para_4) + "<BR><BR>";

        content += "<b>DEFINITION</b><BR><BR>";
        content += getResources().getString(R.string.definition_para_1) + "<BR><BR>";
        content += getResources().getString(R.string.definition_para_2) + "<BR><BR>";

        content += "<b>ACCEPTANCE OF TERMS & CONDITIONS</b><BR><BR>";
        content += getResources().getString(R.string.acceptance_para_1) + "<BR><BR>";

        content += "<b>MODIFICATIONS</b><BR><BR>";
        content += getResources().getString(R.string.modification_para_1) + "<BR><BR>";

        content += "<b>PRIVACY POLICY</b><BR><BR>";
        content += getResources().getString(R.string.privacy_para_1) + "<BR><BR>";

        content += "<b>ELIGIBILITY</b><BR><BR>";
        content += getResources().getString(R.string.eligibility_para_1) + "<BR><BR>";

        content += "<b>txtBravo SERVICES</b><BR><BR>";
        content += getResources().getString(R.string.services_para_1) + "<BR><BR>";

        content += "<b>USE OF txtBravo  PLATFORM</b><BR><BR>";
        content += getResources().getString(R.string.platform_para_1) + "<BR><BR>";

        content += "<b>CONTENT</b><BR><BR>";
        content += getResources().getString(R.string.content_para_1) + "<BR><BR>";

        content += "<b>COMMUNICATION</b><BR><BR>";
        content += getResources().getString(R.string.communication_para_1) + "<BR><BR>";
        content += getResources().getString(R.string.communication_para_2) + "<BR><BR>";

        content += "<b>DISCLAIMER OF WARRANTY</b><BR><BR>";
        content += getResources().getString(R.string.disclaimer_para_1) + "<BR><BR>";

        content += "<b>LIMITATION OF LIABILITY & INDEMNIFICATION</b><BR><BR>";
        content += getResources().getString(R.string.limitations_para_1) + "<BR><BR>";

        content += "<b>INTELLECTUAL PROPERTY (IP) RIGHTS</b><BR><BR>";
        content += getResources().getString(R.string.intellectual_para_1) + "<BR><BR>";

        content += "<b>AMENDMENTS</b><BR><BR>";
        content += getResources().getString(R.string.amendments_para_1) + "<BR><BR>";

        content += "<b>FORCE MAJEURE</b><BR><BR>";
        content += getResources().getString(R.string.force_para_1) + "<BR><BR>";

        content += "<b>SEVERABILITY</b><BR><BR>";
        content += getResources().getString(R.string.severability_para_1) + "<BR><BR>";

        content += "<b>ASSIGNMENT</b><BR><BR>";
        content += getResources().getString(R.string.assignment_para_1) + "<BR><BR>";

        content += "<b>SURVIVAL</b><BR><BR>";
        content += getResources().getString(R.string.survival_para_1) + "<BR><BR>";

        content += "<b>GOVERNING LAW & JURISDICTION</b><BR><BR>";
        content += getResources().getString(R.string.jurisdiction_para_1) + "<BR><BR>";

        content += "<b>CONTACT US</b><BR><BR>";
        content += getResources().getString(R.string.contact_us_para_1) + "<BR><BR>";
        content += getResources().getString(R.string.contact_us_para_2) + "<BR><BR>";
        content += getResources().getString(R.string.contact_us_para_3) + "<BR><BR>";

        content += "</p></body></html>";


        WebView view = (WebView) findViewById(R.id.textContent);
        view.loadData(content, "text/html", "utf-8");
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case android.R.id.home:
            {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}