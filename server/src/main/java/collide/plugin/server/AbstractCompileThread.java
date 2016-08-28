package collide.plugin.server;

import collide.plugin.server.gwt.CompilerBusyException;
import collide.plugin.server.gwt.CompilerRunner;
import com.google.collide.dto.CodeModule;
import com.google.collide.dto.CompileResponse.CompilerState;
import com.google.collide.dto.server.DtoServerImpls.CompileResponseImpl;
import com.google.collide.plugin.shared.CompiledDirectory;
import com.google.collide.plugin.shared.IsRecompiler;
import com.google.collide.server.shared.launcher.VertxLauncher;
import com.google.collide.server.shared.util.ReflectionChannel;
import com.google.collide.shared.util.DebugUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;

import java.io.IOException;

public abstract class AbstractCompileThread
<CompileType extends CodeModule>
extends Thread
implements CompilerRunner

{

  protected ReflectionChannel io;
  protected boolean working = false;
  protected int port;

  protected IsRecompiler controller;

  protected final VertxLauncher server = new VertxLauncher() {
    @Override
    protected NetServer initialize(Vertx vertx, int port) {
      AbstractCompileThread.this.port = port;
      return super.initialize(vertx, port);
    }
    @Override
    protected void handleBuffer(NetSocket event, Buffer buffer) throws IOException {
      AbstractCompileThread.this.handleBuffer(event, buffer);
    };

  };
  protected CompiledDirectory compileRequest;
  protected CompileResponseImpl status;


  protected boolean isFatal(Exception e) {
    return true;
  }

  protected abstract void handleBuffer(NetSocket event, Buffer buffer) throws IOException;

  protected void initialize(Vertx vertx, int port) {
    //called when this compile is open for business
    if (status == null)
      return;
    status.setCompilerStatus(CompilerState.SERVING);
    status.setPort(port);
    io.send(status.toJson());
  }

  protected synchronized void startOrUpdateProxy(CompiledDirectory impl, IsRecompiler compiler) {
    this.compileRequest = impl;
    this.controller = compiler;
    System.out.println("Starting plugin thread "+getClass());
    server.get()//performs actual initialization in the LazyPojo class
    .setTimer(1000, e-> {
        //keep heap clean; the generated gwt classes will fill permgen quickly if they survive;
        System.gc();
    });

    logger().log(Type.INFO, "Started plugin thread on port "+server.getPort());
    impl.setPort(server.getPort());
  }

  protected abstract TreeLogger logger();

  @Override
  public void setChannel(ClassLoader cl, Object io){
    this.io = new ReflectionChannel(cl, io);
  }
  @Override
  public void setOnDestroy(Object runOnDestroy){
    assert io != null : "You must call .setChannel() before calling .setOnDestroy()." +
        "  Called from "+DebugUtil.getCaller();
    this.io.setOnDestroy(runOnDestroy);
  }

  public void compile(String request) throws CompilerBusyException{
    if (working)
      throw new CompilerBusyException(status.getModule());
    synchronized (getClass()) {
      if (isAlive()){
        //if we're already running, we should notify so we can continue working.
          getClass().notify();//wake up!
      }
      working = true;
      if (!isAlive()){
        start();
      }
    }
  }

}
