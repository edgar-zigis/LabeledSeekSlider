# LabeledSeekSlider [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.bio-matic/labeledseekslider/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.bio-matic/labeledseekslider)

Custom & highly configurable seek slider with sliding intervals, disabled state and every possible setting to tackle!
##### Minimum target SDK: 21

![alt text](https://github.com/edgar-zigis/LabeledSeekSlider/blob/master/sample-slide.gif?raw=true)

### Gradle
Make sure you have **Maven Central** included in your gradle repositories.

```gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
```
```gradle
implementation 'com.bio-matic:labeledseekslider:1.3.3'
```
### Usage
``` xml
<com.zigis.labeledseekslider.LabeledSeekSlider
    android:id="@+id/seekSlider"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:lss_activeTrackColor="#FF2400"
    app:lss_bubbleOutlineColor="#E8E8E8"
    app:lss_bubbleValueTextColor="#1A1A1A"
    app:lss_bubbleValueTextFont="@font/ttnorms_bold"
    app:lss_bubbleValueTextSize="14sp"
    app:lss_defaultValue="550"
    app:lss_hideRangeIndicators="false"
    app:lss_inactiveTrackColor="#E8E8E8"
    app:lss_isDisabled="false"
    app:lss_limitValue="850"
    app:lss_limitValueIndicator="Max"
    app:lss_maxValue="1000"
    app:lss_minValue="100"
    app:lss_rangeValueTextColor="#9FA7AD"
    app:lss_rangeValueTextFont="@font/ttnorms_regular"
    app:lss_rangeValueTextSize="12sp"
    app:lss_slidingInterval="50"
    app:lss_thumbSliderBackgroundColor="#FFFFFF"
    app:lss_thumbSliderDrawable="@drawable/ic_thumb_slider"
    app:lss_thumbSliderRadius="12dp"
    app:lss_title="Amount"
    app:lss_titleTextColor="#9FA7AD"
    app:lss_titleTextFont="@font/ttnorms_regular"
    app:lss_titleTextSize="12sp"
    app:lss_trackHeight="4dp"
    app:lss_unit="€"
    app:lss_unitPosition="back"
    app:lss_vibrateOnLimitReached="true"
    app:lss_hideBubble="false" />
```
if you wish to skip certain values, you can set them programatically
```kotlin
seekSlider.valuesToSkip = listOf(4, 6, 10)
```
### Remarks
At the moment wrap_content height configuration falls back to **98dp**, so if you have increased default dimensions, you will also need to increase height param.
