package fisk.chipcloud;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import eu.fiskur.chipcloud.R;

@SuppressWarnings({ "unused", "SameParameterValue" })
public class ChipCloud
    implements View.OnClickListener
{

  private static final boolean USER_CLICK = true;

  private static final boolean AUTO_CHECK = false;

  public enum SelectMode
  {
    multi,
    single,
    mandatory,
    close,
    none
  }

  private final Context context;

  private final ViewGroup layout;

  private final SelectMode selectMode;

  private Typeface typeface = null;

  private StateListDrawable customDrawable = null;

  private ChipCloudConfig config = null;

  private ChipListener chipListener;

  private ChipDeletedListener deletedListener;

  private boolean ignoreAutoChecks = false;

  private Drawable closeX = null;

  public ChipCloud(Context context, ViewGroup layout)
  {
    this.context = context;
    this.layout = layout;
    selectMode = SelectMode.multi;
  }

  public ChipCloud(Context context, ViewGroup layout, ChipCloudConfig config)
  {
    this.context = context;
    this.layout = layout;
    selectMode = config.selectMode;
    this.config = config;
  }

  public void setListener(ChipListener chipListener)
  {
    this.chipListener = chipListener;
  }

  public void setDeleteListener(ChipDeletedListener deletedListener)
  {
    this.deletedListener = deletedListener;
  }

  public void setListener(ChipListener chipListener, boolean ignoreAutoChecks)
  {
    this.chipListener = chipListener;
    this.ignoreAutoChecks = ignoreAutoChecks;
  }

  public void addChips(List<Chippable> objects)
  {
    for (Chippable object : objects)
    {
      addChip(object, null, false);
    }
  }

  public void addChips(Chippable[] objects, Drawable drawable, boolean overrideHeight)
  {
    for (Chippable object : objects)
    {
      addChip(object, drawable, overrideHeight);
    }
  }

  public void addChips(Chippable[] objects)
  {
    for (Chippable object : objects)
    {
      addChip(object, null, false);
    }
  }

  public void addChips(List<Chippable> objects, Drawable drawable, boolean overrideHeight)
  {
    for (Chippable object : objects)
    {
      addChip(object, drawable, overrideHeight);
    }
  }

  public void addChipNoResize(Chippable object, Drawable drawable)
  {
    addChip(object, drawable, false, false);
  }

  public void addChip(Chippable object, Drawable drawable, boolean overrideHeight)
  {
    addChip(object, drawable, true, overrideHeight);
  }

  public void addChip(Chippable object, Drawable drawable)
  {
    addChip(object, drawable, true, false);
  }

  // Custom layout do not work with insetPadding
  public <T> void addChip(Chippable object, Drawable drawable, boolean resizeDrawable,
      boolean overrideHeight)
  {
    ToggleChip toggleChip;
    int chipHeight;
    if (config.useInsetPadding)
    {
      toggleChip = (ToggleChip) LayoutInflater.from(context).inflate(R.layout.inset_toggle_chip, layout, false);
      if (overrideHeight == false)
      {
        chipHeight = context.getResources().getDimensionPixelSize(R.dimen.inset_chip_height);
        toggleChip.setHeight(chipHeight);
      }
    }
    else
    {
      toggleChip = (ToggleChip) LayoutInflater.from(context).inflate(config.customLayout != -1 ? config.customLayout : R.layout.toggle_chip, layout, false);
      if (overrideHeight == false)
      {
        chipHeight = context.getResources().getDimensionPixelSize(R.dimen.chip_height);
        toggleChip.setHeight(chipHeight);
      }
    }
    toggleChip.setLabel(object);

    ConfigHelper.initialise(toggleChip, config);

    if (drawable != null)
    {
      if (resizeDrawable)
      {
        toggleChip.setDrawable(context, drawable);
      }
      else
      {
        toggleChip.setDrawableNoResize(context, drawable);
      }
    }

    if (config.selectMode == SelectMode.close)
    {
      if (closeX == null)
      {
        closeX = ConfigHelper.closeDrawable(context, config.closeTint);
      }
      toggleChip.showClose(closeX);
    }
    toggleChip.setOnClickListener(this);


    layout.addView(toggleChip);
  }

  public void addChip(Chippable object)
  {
    addChip(object, null, false);
  }

  public void setChecked(int index)
  {
    ToggleChip toggleChip = (ToggleChip) layout.getChildAt(index);
    check(toggleChip, true, AUTO_CHECK);
    if (selectMode == SelectMode.single || selectMode == SelectMode.mandatory)
    {
      int childCount = layout.getChildCount();
      for (int i = 0; i < childCount; i++)
      {
        View child = layout.getChildAt(i);
        if (child != toggleChip)
        {
          ToggleChip otherChip = (ToggleChip) child;
          check(otherChip, false, AUTO_CHECK);
        }
      }
    }
  }

  public void setSelectedIndexes(int[] indexes)
  {
    if (selectMode == SelectMode.single || selectMode == SelectMode.mandatory)
    {
      return;
    }

    for (int index : indexes)
    {
      ToggleChip chip = (ToggleChip) layout.getChildAt(index);
      check(chip, true, AUTO_CHECK);
    }
  }

  public void deselectIndex(int index)
  {
    ToggleChip toggleChip = (ToggleChip) layout.getChildAt(index);
    switch (selectMode)
    {
      case multi:
      case single:
        check(toggleChip, false, AUTO_CHECK);
        break;
      default:
        //
    }
  }

  public String getLabel(int index)
  {
    return ((ToggleChip) layout.getChildAt(index)).getText().toString();
  }

  @Override
  public void onClick(View view)
  {
    ToggleChip clickedChip = (ToggleChip) view;
    switch (selectMode)
    {
      case multi:
        check(clickedChip, !clickedChip.isChecked(), USER_CLICK);
        break;
      case single:
        check(clickedChip, !clickedChip.isChecked(), USER_CLICK);
        if (clickedChip.isChecked())
        {
          int childCount = layout.getChildCount();
          for (int i = 0; i < childCount; i++)
          {
            View child = layout.getChildAt(i);
            if (child != clickedChip)
            {
              ToggleChip otherChip = (ToggleChip) child;
              check(otherChip, false, AUTO_CHECK);
            }
          }
        }
        break;
      case mandatory:
        if (!clickedChip.isChecked())
        {
          check(clickedChip, true, USER_CLICK);
          int childCount = layout.getChildCount();
          for (int i = 0; i < childCount; i++)
          {
            View child = layout.getChildAt(i);
            if (child != clickedChip)
            {
              ToggleChip otherChip = (ToggleChip) child;
              check(otherChip, false, AUTO_CHECK);
            }
          }
        }
        break;
      case close:
        final int index = layout.indexOfChild(view);
        final ToggleChip deletedChip = (ToggleChip) view;
        if (config.closeAnimationPeriod != -1)
        {
          AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
          anim.setDuration(config.closeAnimationPeriod);
          anim.setAnimationListener(new Animation.AnimationListener()
          {
            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
              if (deletedListener != null)
              {
                deletedListener.chipDeleted(index, deletedChip.getText().toString());
              }
              layout.removeView(deletedChip);
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
          });
          view.startAnimation(anim);
        }
        else
        {
          if (deletedListener != null)
          {
            deletedListener.chipDeleted(index, deletedChip.getText().toString());
          }
          layout.removeView(deletedChip);
        }

        break;
      case none:
      default:
        //do nothing
    }
  }

  public ViewGroup getLayout()
  {
    return layout;
  }

  private void check(ToggleChip toggleChip, boolean checked, boolean isUserClick)
  {
    toggleChip.setChecked(checked);
    ConfigHelper.update(toggleChip, config);

    if (chipListener != null)
    {
      if (!isUserClick && ignoreAutoChecks)
      {
        return;
      }
      int index = layout.indexOfChild(toggleChip);
      chipListener.chipCheckedChange(index, checked, isUserClick, toggleChip.getLabel());
    }
  }
}