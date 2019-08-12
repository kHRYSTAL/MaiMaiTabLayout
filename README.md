### MaiMaiTabLayout

the library is a tabLayout, can show scale when horizontal scroll `Viewpager`, can show scale scale when vertical scroll `NestedScrollingChild` when this in `AppBarLayout`

### screenshot

![MaiMaiTabLayout](https://github.com/kHRYSTAL/MaiMaiTabLayout/blob/master/screenshot/screenshot.gif)

### usage
see demo

if you need scale tabLayout tab in vertical scroll, you must get appbarLayout
max offset, eg:

```
@Override
public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        // in xml set max offset is 15dp(xhdpi 45px), and tabLayout max scale is getZoomMax=0.4
        // so zoom / offset = 0.4 / 45px
        float zoom = Math.abs(offset) * tabLayout.getZoomMax() / dip2px(MainActivity.this, 15);
        tabLayout.updateSelectTabScale(zoom); // control the tab scale
    }
```

### License

Copyright (C) 2019 kHRYSTAL

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
