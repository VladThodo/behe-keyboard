package com.vlath.keyboard.setup;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.marcoscg.easyabout.EasyAboutFragment;
import com.marcoscg.easyabout.helpers.AboutItemBuilder;
import com.marcoscg.easyabout.items.AboutCard;
import com.marcoscg.easyabout.items.AboutItem;
import com.marcoscg.easyabout.items.NormalAboutItem;
import com.vlath.keyboard.R;

public class AboutFragment extends EasyAboutFragment {

    @Override
    protected void configureFragment(final Context context, View rootView, Bundle savedInstanceState) {
        rootView.setBackgroundColor(getResources().getColor(R.color.gray));
        addCard(new AboutCard.Builder(context)

                .addItem(AboutItemBuilder.generateAppTitleItem(context)
                        .setSubtitle("by @VladThodo"))
                .addItem(AboutItemBuilder.generateAppVersionItem(context, true)
                        .setIcon(R.drawable.ic_info_outline))
                .addItem(new NormalAboutItem.Builder(context)
                        .setTitle("Licenses")
                        .setIcon(R.drawable.ic_license)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        })
                        .build())

                .build());

        addCard(new AboutCard.Builder(context)
                .setTitle("Author")
                .addItem(AboutItemBuilder.generateAppTitleItem(context)
                        .setTitle("Vlad Todosin")
                        .setSubtitle("@VladThodo")
                        .setIcon(R.drawable.ic_author))
                .addItem(AboutItemBuilder.generateLinkItem(context, "https://github.com/marcoscgdev/EasyAbout/issues/new")
                        .setTitle("Fork me on GitHub")
                        .setIcon(R.drawable.ic_social_github))
                .addItem(AboutItemBuilder.generateLinkItem(context, "https://")
                        .setTitle("Visit the official website")
                        .setIcon(R.drawable.ic_web))
                .addItem(AboutItemBuilder.generateEmailItem(context, "vladtodosin@gmail.com")
                        .setIcon(R.drawable.ic_mail)
                        .setTitle("E-mail me"))
                .build());

        addCard(new AboutCard.Builder(context)
                .setTitle("Support")
                .addItem(AboutItemBuilder.generatePlayStoreItem(context)
                        .setTitle("Rate application")
                        .setIcon(R.drawable.ic_rate))
                .addItem(AboutItemBuilder.generateLinkItem(context, "https://github.com/marcoscgdev/EasyAbout/issues/new")
                        .setTitle("Report bugs")
                        .setIcon(R.drawable.ic_bug))
                        .build());
    }
}