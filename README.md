## javafx编写的电脑客户端的软件，协助日常的上班工作  
***[项目的GitHub路径](https://github.com/mwrmaven/HelloFx-Maven)***  
***[项目的Gitee路径](https://gitee.com/mwrmaven/HelloFx-Maven)***  
***github中的图片可能无法显示，可下载代码或跳转到Gitee路径查看README.md***
> ***软件中包含的功能***
>  * 文件中的文本行排序
>  * URL编码批量转换
>  * 批量修改文件名
>  * 批量递增标识修改文件名
>  * 下载公众号文章中的音视频
>  * 比较两个文件内容
>  * 文本批量生成

### 功能介绍  
### ***一、文件中的文本行排序***  
![](images/markdown/linesort.png)
> 该功能可以为单个文件或文件夹下所有的文件的内容按行进行排序。
>
> 1、其中单文件或文件夹的选择按实际需求选择即可。
>
> 2、查找行中排序关键字的方式，分为分隔符方式查找和字符范围查找；
>
> * 分隔符方式查找意为以某个关键字符为准，将数据行进行分割成一段一段的，例如：  
> ![](images/markdown/sort_test_resource.png)  
> 上图中的行数据使用“|”进行分割后，再指定关键字位置，例如关键字位置为 2，以此进行排序，
> 得到的结果如下图：  
> ![](images/markdown/sort_test_target.png)
>
> * 字符范围查找意为根据开始字符和结束字符找到指定的字符范围，并以该范围内的字符进行排序；  
> 仍以上面的数据为例：  
> ![](images/markdown/sort_test_resource.png)  
> 指定字符的开始位置为4，结束位置为5，即“30”、“50”、“40”所在的列，进行排序，得到结果如下图：  
> ![](images/markdown/sort_test_target.png)  
>
> 3、按需选择关键字的排序方式；
>
> 4、结果文件路径与原文件的相同，结果文件名称为“原文件名称_new.xxx”；  

### ***二、URL编码批量转换***  
![](images/markdown/urlconvert.png)
> 该功能主要将正常的字符串转换为url编码格式 或者 将url编码格式的字符串解码为正常字符串；
> 
> 1、源数据分为'单个url转换'、'单个文件中url批量转换'、'文件夹下所有文件中url批量转换'；
> 
> 2、其中'单个url转换'是直接在输入框中输入单条源数据；  
> 当输入单个url时，可以直接点击'批量处理'对源数据进行编解码；  
> 如图所示：
> ![](images/markdown/url_convert_one_url.png)
> 
> 3、另外两个为选择包含多条源数据的文件或文件夹，这里以'单个文件中url批量转换'功能为例；  
> * 获取文件或选择文件夹路径部分，直接点击选择源文件(夹)即可；
> * 在选择源文件之后，需要指定源文件的格式；  
> 如果选择excel格式文件，则需要指定url所在的列；  
> 同时可以选择需要从excel文件中拷贝出来的列；  
> 如图所示：  
> ![](images/markdown/url_convert_excel_columns.png)  
> * 编译前字url后面追加的固定字符(在后置字符前)，例如设置为 a；
> 编译前在url上追加的字符：前置字符，例如设置为 b；后置字符，例如设置为 c；  
> 则编译前的源数据会拼接为：b源数据ac
> * 编译后在url前面追加的固定字符(在前置字符前)，例如设置为 j；  
> 编译后在url上追加的字符：前置字符，例如设置为 k；后置字符，例如设置为 m；  
> 则编译后的数据会拼接为：jk编译后的数据m  
> * '删除选中项'功能可以删除编译前在url后面追加的固定字符的记录；
> 注意，这里需要先选择下拉框中要删除的数据，再点击'删除选中项'按钮；
> 
> 4、选择模板文件部分  
> 处理后的结果可以按要求输出到指定的模板文件，模板文件中的变量使用 ${变量} 的方式表示； 
> 模板中的文本可以设置'底色'、'字体'、'字体颜色'等，输出的结果文件会按模板样式输出； 
> 例如：  
> ![](images/markdown/url_convert_template_path.png)  
> 模板中的文本内容如下：
> ![](images/markdown/url_convert_template.png)  
> 注意：
> * 模板文件中的 ${原URL} 和 ${处理后的URL} 为固定的源数据和转换后的数据；  
> * 其余的变量名称需要和'请选择需要拷贝出来的列（可多选）'中选择的列名对应；

### ***三、批量修改文件名***  
![](images/markdown/batchEditFileName.png)  
> 该功能可以批量修改指定文件夹下匹配到的文件的名称；  
> * 选择指定的文件夹（即要修改的文件的所属文件夹）；
> * 输入文件名称中的旧字符，以及要替换旧字符的新字符；  
> ![](images/markdown/batchEditFileName_pre.png)
> * 点击'批量修改后'，效果为：  
> 原文件夹下的源文件如图：  
> ![](images/markdown/batchEditFileName_resource.png)  
> 执行批量修改操作后如图：
> ![](images/markdown/batchEditFileName_result.png)  
> **注意：这里只会修改可以匹配到旧字符的文件名称，而未匹配到的文件名称不会被修改。**

### ***四、批量递增标识修改文件名***  
![](images/markdown/batchIncrementEdit.png)  
> 该功能是在'批量修改文件名'功能上的更新，可以指定一定的规律来修改文件名称；
> * '文件名中插入或替换'可以选择后置插入（在文件名末尾添加信息）、
> 前置插入（在文件名开头添加信息）、替换字符（替换文件名中的字符）；
> * 该功能中可以选择'根据模板文件创建新文件'或'只替换文件名'；  
> 如果选择根据模板文件创建文件，则需要选择模板文件，而对文件名的操作则是根据模板文件名为基础进行操作的；  
> 如果选择只替换文件名，则只会操作指定文件夹下现有的文件的文件名，不会创建新文件；  
> * 初始标识符的类型指的在文件名中替换或插入的基准标识符，当前只有数字和时间两种类型；
> * 递增标识的配置是在初始标识符的基础上做的扩展以及按规律的变更，如图：  
> ![](images/markdown/batchIncrementEdit_string.png)  
> 图中配置的：后置插入、只替换文件名、初始标识符为数字类型、初始字符3，
> 初始字符的前置字符pre，初始字符的后置字符after，初始字符的步长 1；  
> 修改前的源文件图：  
> ![](images/markdown/batchIncrementEdit_resource.png)  
> 修改后的效果图：  
> ![](images/markdown/batchIncrementEdit_result.png)  

### ***五、下载公众号文章中的音视频***  
![](images/markdown/downloadVideoFromArticle.png)  
> 该功能可以从公众号文章中自动或者音频、视频，并下载（当前功能只能下载简单路径的音视频，复杂路径的音视频暂时无法处理）；  
> * 将公众号的链接添加到'请输入公众号文章的url'输入框中；
> * 选择保存音视频的文件夹路径；  
> 如图所示：  
> ![](images/markdown/downloadVideoFromArticle_result.png)  

### ***六、比较两个文件的内容***  
![](images/markdown/fileCompare.png)  
> 该功能可以比较两个文件中的文本的不同
> * 注意：  
> 1、当前只支持txt和doc、docx三种文本格式；  
> 2、只比较文件中的文本内容；
> * 根据需求选择两个文件，点击'比较'，不同的文本会标红显示；   
> 如图所示：
> ![](images/markdown/fileCompare_result.png)  

### ***七、文本批量生成***  
![](images/markdown/textBatchCreate.png)  
> 该功能可以根据文本模板批量生成文本信息；  
> * 在文本模板输入框中，输入需要批量生成的文本，其中包含的动态文本部分使用文本参数替代；  
> 如图所示：  
> ![](images/markdown/textBatchCreate_template.png)  
> 其中参数使用 ${xxx} 的格式配置，图中的文本参数为 ${param1}、${param2}；  
> * 点击配置文本参数，可以自动抓取到文本模板中的参数，然后对不同的文本参数配置不同的参数值；  
> 例如：批量生成10个文本，则需要在参数值中输入你所需要的10个动态值，多个值之间用**英文逗号**分隔;  
> **注意：当配置多个文本参数时，每个文本参数的参数值个数需要一致**；  
> 如图所示：  
> ![](images/markdown/textBatchCreate_para.png)  
> * 选择结果文件的输出路径；
> 功能示例：  
> ![](images/markdown/textBatchCreate_result.png)