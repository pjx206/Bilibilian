package WebCrawler.Tasks;


/**
 * 爬取任务基类，可以看UserInfoTask.java是怎么继承来实现爬取功能的
 */
public abstract class Task {

    public abstract void crawl();

    protected abstract void toNextTarget();

    protected abstract void saveCrawResult(Object result);

    protected abstract Object[] getStorage();

    // 检查爬取进度是否达到目标
    public abstract boolean taskFinished();

    public abstract String getProgressDescription();
}
