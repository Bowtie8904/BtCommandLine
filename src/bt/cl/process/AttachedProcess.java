package bt.cl.process;

import bt.log.Log;
import bt.types.Killable;
import bt.utils.Array;
import bt.utils.Exceptions;
import bt.utils.Null;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class AttachedProcess implements Killable
{
    private static final String DEFAULT_EXIT_COMMAND = "exit\n";
    private static String[] fileEndings = { "", ".bat", ".exe", ".jar" };
    private Process process;
    private Consumer<String> incominTextConsumer;
    private BufferedWriter out;
    private String executablePath;
    private String executable;
    private String[] args;
    private boolean isAlive;

    public AttachedProcess(String... args)
    {
        this.executablePath = findAbsolutePath(args[0]);
        this.args = Array.pop(args, 0);
    }

    public String findAbsolutePath(String executable)
    {
        Log.entry(executable);

        File file = new File(executable);

        if (!file.exists() || !file.canExecute())
        {
            String path = System.getenv("PATH");
            String[] dirs = path.split(";");

            for (String dir : dirs)
            {
                for (String ending : fileEndings)
                {
                    Path pathToFile = Paths.get(dir, executable + ending);
                    file = new File(pathToFile.toString());

                    if (file.canExecute())
                    {
                        this.executable = executable + ending;
                        break;
                    }
                    else
                    {
                        file = null;
                    }
                }

                if (file != null)
                {
                    break;
                }
            }
        }
        else
        {
            file = null;
        }

        if (file == null)
        {
            throw new IllegalArgumentException("Cant find executable command for '" + executable + "'.");
        }

        String result = file.getAbsolutePath();

        Log.exit(result);

        return result;
    }

    public int start() throws IOException, InterruptedException
    {
        Log.entry();

        ProcessBuilder processBuilder = new ProcessBuilder(Array.concat(new String[] { this.executablePath }, this.args, String[]::new));
        processBuilder.redirectErrorStream(true);
        this.process = processBuilder.start();
        this.out = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));
        this.isAlive = true;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(this.process.getInputStream())))
        {
            String line = null;

            while ((line = in.readLine()) != null)
            {
                Log.debug("Received line from process '{}'", line);
                Null.checkConsume(this.incominTextConsumer, line + "\n");
            }
        }

        this.process.waitFor();
        kill();

        int exitCode = this.process.exitValue();

        Log.exit(exitCode);

        return exitCode;
    }

    public void write(String text) throws IOException
    {
        Log.entry(text);

        this.out.write(text);
        this.out.flush();

        Log.exit();
    }

    public void setIncominTextConsumer(Consumer<String> incominTextConsumer)
    {
        this.incominTextConsumer = incominTextConsumer;
    }

    @Override
    public void kill()
    {
        Log.entry();

        if (this.process != null && this.process.isAlive())
        {
            Log.debug("Sending default exit command to process '{}'", this.executable);
            Exceptions.uncheck(() -> write(DEFAULT_EXIT_COMMAND));
        }

        Exceptions.uncheck(() -> Null.checkClose(this.out));
        Null.checkRun(this.process, () -> this.process.destroy());
        this.isAlive = false;

        Log.exit();
    }

    public boolean isAlive()
    {
        return this.isAlive;
    }

    public String getExecutable()
    {
        return executable;
    }

    public String getExecutablePath()
    {
        return executablePath;
    }

    public String[] getArgs()
    {
        return args;
    }
}