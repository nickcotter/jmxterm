package org.cyclopsgroup.jmxterm.jdk6;

import org.apache.commons.lang.Validate;
import org.cyclopsgroup.jmxterm.JavaProcess;
import org.cyclopsgroup.jmxterm.JavaProcessManager;
import org.cyclopsgroup.jmxterm.utils.WeakCastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JDK6 specific java process manager
 * 
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
public class Jdk6JavaProcessManager
    extends JavaProcessManager
{
    private final StaticLocalVirtualMachine staticVm;

    /**
     * @param classLoader ClassLoader to load JDK internal classes
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     */
    public Jdk6JavaProcessManager( ClassLoader classLoader )
        throws SecurityException, NoSuchMethodException, ClassNotFoundException
    {
        Validate.notNull( classLoader, "ClassLoader can't be NULL" );
        Class<?> originalClass = classLoader.loadClass( LocalVirtualMachine.ORIGINAL_CLASS_NAME );
        staticVm = WeakCastUtils.staticCast( originalClass, StaticLocalVirtualMachine.class );
    }

    /**
     * @inheritDoc
     */
    @Override
    public JavaProcess get( int pid )
    {
        Map<Integer, Object> lvms = staticVm.getAllVirtualMachines();
        Object vm = lvms.get( pid );
        if ( vm == null )
        {
            return null;
        }
        return getJavaProcess(vm);
    }

    private JavaProcess getJavaProcess(Object vm) {
        try
        {
            return new Jdk6JavaProcess( WeakCastUtils.cast( vm, LocalVirtualMachine.class ) );
        }
        catch ( SecurityException e )
        {
            throw new RuntimeException( "Can't cast " + vm + " to LocalVirtualMachine", e );
        }
        catch ( NoSuchMethodException e )
        {
            throw new RuntimeException( "Can't cast " + vm + " to LocalVirtualMachine", e );
        }
    }

    @Override
    public JavaProcess get(String processName) {

        Map<Integer, Object> lvms = staticVm.getAllVirtualMachines();

        for (Object jvmObject : lvms.values()) {

            JavaProcess javaProcess = getJavaProcess(jvmObject);

            if(javaProcess.getDisplayName().contains(processName)) {
                return javaProcess;
            }
        }

        return null;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<JavaProcess> list()
    {
        Map<Integer, Object> lvms = staticVm.getAllVirtualMachines();
        List<JavaProcess> result = new ArrayList<JavaProcess>( lvms.size() );
        for ( Object lvm : lvms.values() )
        {
            LocalVirtualMachine vm;
            try
            {
                vm = WeakCastUtils.cast( lvm, LocalVirtualMachine.class );
                result.add( new Jdk6JavaProcess( vm ) );
            }
            catch ( SecurityException e )
            {
                throw new RuntimeException( "Can't cast " + lvm + " to LocalVirtualMachine", e );
            }
            catch ( NoSuchMethodException e )
            {
                throw new RuntimeException( "Can't cast " + lvm + " to LocalVirtualMachine", e );
            }
        }
        return result;
    }

}
