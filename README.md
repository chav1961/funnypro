# funnypro

Очередная моя шиза - интерпретатор языка Prolog. Состав предикатов и опреаторов соответствует описанию Братко.
Интерпретатор может запускаться как отдельное приложение, а также встраиваться в Java по стандартному SPI. 
Приведу в качестве примера код класса chav1961.funnypro.Application:

public class Application {
	public static void main(String[] args) throws ScriptException {
		try(final Reader	in = new InputStreamReader(System.in);
			final Writer	out = new OutputStreamWriter(System.out); 
			final Writer	err = new OutputStreamWriter(System.err)) {
			
 			final ScriptEngineManager 	factory = new ScriptEngineManager();
			final ScriptEngine 			engine = factory.getEngineByName("FunnyProlog");

 			((FunnyProEngine)engine).console(in,out,err);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}

Более подробное описание см Вики к проекту. Никаких объектных фич, обработки исключений и прочего ООП-бреда в интерпретаторе
нет и не будет - для этого есть прекрасный язык Java (на котором интерпретатор и написан).
