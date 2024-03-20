/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.common.view;

import com.common.util.ResourceUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class KeyButton extends Button {

	// TODO: Get rid of this

	public KeyButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public KeyButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		Drawable d = context.getDrawable(ResourceUtil
				.getDrawableId(context, "button_common_keyview"));
		if (d != null) {
			setBackground(d);
		} else {
			setBackground(new KeyButtonRipple(context, this));
		}
	}

//	@Override
//	protected void onWindowVisibilityChanged(int visibility) {
//		super.onWindowVisibilityChanged(visibility);
//		if (visibility != View.VISIBLE) {
//			jumpDrawablesToCurrentState();
//		}
//	}

}
