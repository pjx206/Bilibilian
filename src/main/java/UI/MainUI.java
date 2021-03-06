package UI;

import UI.menuType.OptionsListMenu;
import WebCrawler.CrawlerManager;
import WebCrawler.TaskRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;


public class MainUI implements OptionsListMenu {
    private static MainUI instance;

    private CrawlerManager crawlerManager;

    private MainUI() {

    }

    public static MainUI getMainUI() {
        if (instance == null) {
            synchronized (CrawlerManager.class) {
                if (instance == null)
                    instance = new MainUI();
            }
        }
        return instance;
    }

    private boolean haveShowedWelcome = false;
    private void smartShowWelcome() {
        if (haveShowedWelcome) {
            return;
        }
        System.out.println(
                "\n" +
                        "   ██████╗  ██╗ ██╗      ██╗ ██████╗  ██╗ ██╗      ██╗  █████╗  ███╗   ██╗\n" +
                        "   ██╔══██╗ ██║ ██║      ██║ ██╔══██╗ ██║ ██║      ██║ ██╔══██╗ ████╗  ██║\n" +
                        "   ██████╔╝ ██║ ██║      ██║ ██████╔╝ ██║ ██║      ██║ ███████║ ██╔██╗ ██║\n" +
                        "   ██╔══██╗ ██║ ██║      ██║ ██╔══██╗ ██║ ██║      ██║ ██╔══██║ ██║╚██╗██║\n" +
                        "   ██████╔╝ ██║ ███████╗ ██║ ██████╔╝ ██║ ███████╗ ██║ ██║  ██║ ██║ ╚████║\n" +
                        "   ╚═════╝  ╚═╝ ╚══════╝ ╚═╝ ╚═════╝  ╚═╝ ╚══════╝ ╚═╝ ╚═╝  ╚═╝ ╚═╝  ╚═══╝"
        );
        haveShowedWelcome = true;
    }

    @Override
    public String getMenuTitle() {
        return "Main Menu";
    }

    private final String[] options = new String[]{
            "查看当前支持的爬取目标",
            "添加爬取目标到任务列表",
            "执行全部已添加任务",
            "查看任务状态",
            "控制爬取任务",
            "查看已爬取数据",
            "退出Bilibilian",
    };

    @Override
    public String[] getMenuOptions() {
        return options;
    }

    /**
     * 通过一个switch case来处理getMenuOptions里面支持的菜单选项
     *
     * @param optionIdx 选项编号（数组下标）
     */
    @Override
    public void performOption(int optionIdx) {
        assert (optionIdx >= 0 && optionIdx < options.length);
        crawlerManager = CrawlerManager.getCrawlerManager(); // 保证执行选项时，crawlerManager已经拿到实例
        switch (optionIdx) {
            case 0:
                showSupportedTasks();
                break;

            case 1:
                pickIntoTaskRunner();
                break;

            case 2:
                execTaskRunnerList();
                break;

            case 3:
                showTaskRunnersStatus();
                break;

            case 4:
                controlTaskRunner();
                break;

            case 5:
                ShowDataUI showDataUI = ShowDataUI.getShowDataUI();
                showDataUI.handleInteraction();
                break;

            case 6:
                wantToExit = true;
                break;
            default:
                System.out.println("Not implemented");
        }
    }


    private boolean wantToExit = false;

    /* 各个case的具体实现 START */
    private void pressAnyKey() {
        try {
            System.out.println("按回车返回");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // case 0: DONE
    private void showSupportedTasks() {
        System.out.println(Arrays.toString(crawlerManager.getSupportedTasksName()));
        pressAnyKey();
    }

    // case 1
    private void pickIntoTaskRunner() {
        PickTasksUI pickTaskUI = PickTasksUI.getPickTaskUI();
        pickTaskUI.handleInteraction();
        int[] result = pickTaskUI.getPickResult();
        if (result.length == 0){
            // 没有勾选任何要添加的任务
            System.out.println("没有添加任何爬取任务");
            return;
        }
        System.out.println("添加了" + result.length + "个任务");
//        System.out.println("请对添加的任务进行配置");
        for (int j : result) {
            crawlerManager.buildTaskRunnerFromTaskId(j); // 之前写成i了，隐秘bug
        }
    }

    // case 2: DONE
    private void execTaskRunnerList() {
        if (crawlerManager.getRunnersList().size() == 0) {
            System.out.println("尚未添加爬取任务");
            return;
        }
        crawlerManager.commitAllTaskRunners();
    }

    // case 3: DONE
    private void showRunnersList() {
        TaskRunner[] runnersList = crawlerManager.getRunnersList().toArray(new TaskRunner[0]);
        if (runnersList.length == 0) {
            System.out.println("尚无添加爬取任务");
            return;
        }
        System.out.println("Index       Name          Status");
        for (int i = 0; i < runnersList.length; i++) {
            TaskRunner runner = runnersList[i];
            String taskName = runner.getTaskName();

            System.out.printf("%-4d %15s  %15s\n",
                    i,
                    taskName,
                    runner.getStatusDescription()
            );
        }
    }

    private void showTaskRunnersStatus() {
        showRunnersList();
        pressAnyKey();
    }


    // case 4
    private void controlTaskRunner() {
        // 展示可以用于操作的Runner
        showRunnersList();
        if (crawlerManager.getRunnersList().size() == 0) {
            return;
        }
        System.out.println("请输入要执行操作的任务编号");
        Scanner scan = new Scanner(System.in);
        while (true) {
            try {
                int idx = Integer.parseInt(scan.nextLine());
                if (idx >= 0 && idx < crawlerManager.getRunnersList().size()) {
                    System.out.printf("你选择了编号为%d的爬取任务\n", idx);
                    TaskRunnerControlUI ui = TaskRunnerControlUI.getControlUI();
                    ui.setTaskRunnerId(idx);
                    ui.handleInteraction();
                    break;
                } else {
                    System.out.println("输入数字范围错误，请重试");
                }
            } catch (NumberFormatException e) {
                System.out.println("请输入数字！");
            }
        }
    }

    // case 5

    // case 6: DONE
    public boolean isWantToExit() {
        return wantToExit;
    }

    /* 各个case的具体实现 END */


    @Override
    public void handleInteraction() {
        smartShowWelcome();
        Scanner cmdScan = new Scanner(System.in);
        do {
            showListMenu();

            int option = 0;
            do {
                System.out.print("> "); // 命令提示符
                try {
                    option = Integer.parseInt(cmdScan.nextLine()); // 转换成switch-case对应的case
                    option -= 1;
                } catch (NumberFormatException e) {
                    System.out.println("请输入正确的数字");
                    option = -1;
                }
            } while (option < 0 || option >= options.length);
            performOption(option);
        } while (!isWantToExit());
    }

    private void showListMenu() {
        System.out.println("\n* " + this.getMenuTitle() + " *");
        String[] options = getMenuOptions();
        for (int i = 1; i <= options.length; i++) {
            System.out.printf("[%d] %s\n", i, options[i-1]);
        }
    }
}
