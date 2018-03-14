# Picture
图片选择，图片截取！图片多选择，图片预览，图片快速取消选择，并且适配7.0系统
==============================
自从7.0系统的发布后！android加入了私有文件化的新特性！在那里，如果在兼容的系统版本升到7.0以上！在拍照功能还是以以前的方式进行操作，则会到时程序崩溃！
并且在android自带的图片选择器中，比较简陋！所以就封了一个拍照，选择图片等一系列的功能的类库

废话不多说！上教程！贴代码！具体的在使用中体验
---------------------------
*第一步！都一样！接入类库！*
1、在项目的gradle中加入代码
--java
repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
---
2、在app的gradle下加入代码，接入我的类库
---java
compile 'com.github.Break369:Picture:v1.2'
-----
3、在这里我使用的是glide图片加载框架！所以我就在app的gradle下面也加了
--java
compile 'com.github.bumptech.glide:glide:3.6.1'
---
当然！这网路加载框架！可以根据自己的爱好进行选择！对使用没有影响！只要在加载的时候修改就ok

*第二步，在定义xml文件，设置图片选择后显示的容器
---java
<LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        <LinearLayout
            android:id="@+id/lyContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"/>
        <!--<ImageView
            android:id="@+id/imgAdd"
            android:layout_width="80dp"
            android:layout_height="80dp"
            android:layout_marginLeft="15dp"
            android:gravity="right"
            android:layout_marginRight="15dp"
            android:layout_marginTop="5dp"
            android:src="@drawable/test_img_wall_first" />-->
    </LinearLayout>
    ------
   *第二步，定义图片加载，这个和你选择的图片加载器有关*
   在代码中创建一个java类：GlideLoader实现ImageLoader接口
   ---java
   public class GlideLoader implements ImageLoader {

	@Override
    public void displayImage(Context context, String path, ImageView imageView) {
        Glide.with(context)
                .load(path)
                .placeholder(R.drawable.global_img_default)
                .centerCrop()
                .into(imageView);
    }

}
----
*第四步，初始化选择器的参数*
在代码中
---java
private void initImageConfig() {
        mImageConfig = new ImageConfig.Builder(
                new GlideLoader())
                .steepToolBarColor(getResources().getColor(R.color.titleBlue))
                .titleBgColor(getResources().getColor(R.color.titleBlue))
                .titleSubmitTextColor(getResources().getColor(R.color.white))
                .titleTextColor(getResources().getColor(R.color.white))
                // 开启多选   （默认为多选）
                .mutiSelect()
                // 多选时的最大数量   （默认 9 张）
                .mutiSelectMaxSize(9)
                //设置图片显示容器，参数：、（容器，每行显示数量，是否可删除）
                .setContainer(lyContainer, 5, true)
                // 已选择的图片路径
                .pathList(paths)
                // 拍照后存放的图片路径（默认 /temp/picture）
                .filePath("/temp")
                // 开启拍照功能 （默认关闭）
                .showCamera()
                .build();
    }
 ----
 *第五步，选择器的启动
 ---java
  ImageSelector.open(ActivityGWDT.this, mImageConfig);
  ---
  *嗯！以上的就是这个选择器的使用！超级简单！在性能上还行！可以进行预览，根据需求快速反选等等操作，详细浏览等等操作
   


