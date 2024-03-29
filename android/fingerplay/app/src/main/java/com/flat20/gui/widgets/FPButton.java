package com.flat20.gui.widgets;

import com.flat20.gui.Materials;
import com.flat20.gui.sprites.MaterialSprite;
import com.flat20.gui.sprites.Sprite;
import com.flat20.gui.textures.Material;

public class FPButton extends Button {

	protected MaterialSprite mDefault;
	protected MaterialSprite mClicked;
	protected Sprite mIcon;

	protected boolean mIsActive = false;

	public FPButton(Material iconMaterial, int width, int height) {
		super(width, height);

		mDefault = new MaterialSprite(Materials.BUTTON, width, height);
		addSprite(mDefault);

		mClicked = new MaterialSprite(Materials.BUTTON_HIGHLIGHT, width, height);
		mClicked.visible = false;
		addSprite(mClicked);

        //ResourceTexture iconTexture = TextureManager.createResourceTexture(iconResourceId, 32, 32);
        mIcon = new MaterialSprite(iconMaterial);
        addSprite(mIcon);

        setSize(width, height);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);

		mDefault.setSize(width, height);
		mClicked.setSize(width, height);
		int iconSize = (width < height) ? width-16 : height - 16;
        mIcon.setSize(iconSize, iconSize) ;
        mIcon.x = width/2 - iconSize/2;
        mIcon.y = height/2 - iconSize/2;
	}

	public void setActive(boolean active) {
		mIsActive = active;
		if (active) {
			mDefault.visible = false;
			mClicked.visible = true;
		} else {
			mDefault.visible = true;
			mClicked.visible = false;
		}
	}

	@Override
	protected void onPress() {
		mDefault.visible = false;
		mClicked.visible = true;
		super.onPress();
	}

	@Override
	protected void onRelease() {
		if (!mIsActive) {
			mDefault.visible = true;
			mClicked.visible = false;
		}
		super.onRelease();
	}
/*
	@Override
	public void onFocusChanged(boolean focus) {
		super.onFocusChanged(focus);

		if (focus) {
			mDefault.visible = false;
			mClicked.visible = true;
		} else if (!mIsActive) {
			mDefault.visible = true;
			mClicked.visible = false;
		}
	}
*/
}
