package cse403.sp2020.tidy.ui.main;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
  private final List<Fragment> fragmentList = new ArrayList<>();
  private final List<String> fragmentTitleList = new ArrayList<>();

  public SectionsPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  public void addFragment(Fragment fragment, String title) {
    fragmentList.add(fragment);
    fragmentTitleList.add(title);
  }

  /**
   * Returns the fragment at the given position
   * @param position index of the fragment
   * @return the fragment at position
   */
  @Override
  public Fragment getItem(int position) {
    return fragmentList.get(position);
  }

  /**
   * Returns the title at the given position.
   * @param position index of the title
   * @return the title at position
   */
  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    return fragmentTitleList.get(position);
  }

  /**
   * Returns the number of fragments
   * @return the number of fragments
   */
  @Override
  public int getCount() {
    return fragmentList.size();
  }
}
