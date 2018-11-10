package com.example.chihurmnanyanwanevu.bakingapp;
// Note the static imports, which enhance the code clarity by reducing code length
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.chihurmnanyanwanevu.bakingapp.ui.activities.RecipeActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
@LargeTest

public class ExampleInstrumentedTest {
    private static final int STEP_WITH_VIDEO = 0;
    private static final int STEP_WITHOUT_VIDEO = 1;
    // To launch the mentioned activity under testing
    @Rule
    public ActivityTestRule<RecipeActivity> mActivityRule = new ActivityTestRule<>(RecipeActivity.class);

    @Test
    public void testRecipename() {
        // check recipe name
        onView(withId(R.id.recipeName_tv));

    }

    @Test
    public void testRecipecount() {
        // check recipe count
        onView(withId(R.id.recipeSteps_lv));

    }

    @Test
    public void clickOnStepWithVideo_showsVideoPlayerView() {
        onView(withId(R.id.exoPlayerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(STEP_WITH_VIDEO, click()));

        onView(
                allOf(
                        withId(R.id.exoPlayerView),
                        withParent(withParent(withId(R.id.exoPlayerView))),
                        isDisplayed()))
                .check(matches(isDisplayed()));
    }
}
