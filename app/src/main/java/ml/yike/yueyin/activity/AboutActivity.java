package ml.yike.yueyin.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import ml.yike.yueyin.R;

public class AboutActivity extends BaseActivity {

    private Toolbar toolbar;

    private TextView versionText;
    private ListView libraryItem;
    private LinearLayout startLayout, blogLayout, emailLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        init();
    }


    private void init() {
        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }

        versionText = (TextView) findViewById(R.id.about_version);
        emailLayout = (LinearLayout) findViewById(R.id.about_email_ll);
        startLayout = (LinearLayout) findViewById(R.id.about_start_ll);
        blogLayout = (LinearLayout) findViewById(R.id.about_blog_ll);
        libraryItem = (ListView) findViewById(R.id.library_list);
        versionText.setText(getVersion());

        startLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://github.com/yutayouguan/yueMusic");
            }
        });
        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("mailto:wmdyx@outlook.com");
            }

        });
        blogLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUrl("https://yike.ml");
            }

        });
libraryItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
  switch (position){
      case 0:
          openUrl(getString(R.string.library0_url));
          break;
      case 1:
          openUrl(getString(R.string.library1_url));
          break;
      case 2:
          openUrl(getString(R.string.library2_url));
          break;
      case 3:
          openUrl(getString(R.string.library3_url));
          break;
      case 4:
          openUrl(getString(R.string.library4_url));
          break;
      case 5:
          openUrl(getString(R.string.library5_url));
          break;
  }


    }
});

    }


    /**
     * 使用系统浏览器打开
     */
    private void openUrl(String url) {
        /*  if ("@".contains(url)) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(url));
            startActivity(intent);
       } else if ("http".contains(url)) { */
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
//        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }


    /**
     * 获取app版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return this.getString(R.string.version_name) + version;
        } catch (Exception e) {
            e.printStackTrace();
            return this.getString(R.string.can_not_find_version_name);
        }
    }
}
