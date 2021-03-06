package tigase.monitor.modules;

import tigase.component.adhoc.AdHocCommand;
import tigase.component.adhoc.AdHocCommandException;
import tigase.component.adhoc.AdHocResponse;
import tigase.component.adhoc.AdhHocRequest;
import tigase.form.Field;
import tigase.form.Form;
import tigase.kernel.beans.Bean;
import tigase.kernel.beans.Inject;
import tigase.kernel.core.Kernel;
import tigase.monitor.MonitorComponent;
import tigase.monitor.TasksScriptRegistrar;
import tigase.xml.Element;
import tigase.xmpp.Authorization;
import tigase.xmpp.JID;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;

@Bean(name = "x-add-task", parent = MonitorComponent.class, active = true)
public class AddScriptTaskCommand
		implements AdHocCommand {

	@Inject
	private MonitorComponent component;
	@Inject
	private Kernel kernel;

	public AddScriptTaskCommand() {
	}

	@Override
	public void execute(AdhHocRequest request, AdHocResponse response) throws AdHocCommandException {
		try {
			final Element data = request.getCommand().getChild("x", "jabber:x:data");

			if (request.getAction() != null && "cancel".equals(request.getAction())) {
				response.cancelSession();
			} else if (data == null) {
				Form form = new Form("form", "Add monitor script", null);

				List<ScriptEngineFactory> sef = kernel.getInstance(ScriptEngineManager.class).getEngineFactories();
				ArrayList<String> labels = new ArrayList<String>();
				ArrayList<String> values = new ArrayList<String>();
				for (ScriptEngineFactory scriptEngineFactory : sef) {
					labels.add(scriptEngineFactory.getLanguageName());
					values.add(scriptEngineFactory.getExtensions().get(0));
				}

				form.addField(Field.fieldTextSingle("scriptName", "", "Script name"));
				form.addField(
						Field.fieldListSingle("scriptExtension", "", "Script engine", labels.toArray(new String[]{}),
											  values.toArray(new String[]{})));
				form.addField(Field.fieldTextMulti("scriptContent", "", "Script"));

				response.getElements().add(form.getElement());
				response.startSession();
			} else {
				Form form = new Form(data);

				if ("submit".equals(form.getType())) {
					String scriptName = form.getAsString("scriptName");
					String scriptExtension = form.getAsString("scriptExtension");
					String scriptContent = form.getAsString("scriptContent");

					((TasksScriptRegistrar) kernel.getInstance(TasksScriptRegistrar.ID)).registerScript(scriptName,
																										scriptExtension,
																										scriptContent);

				}
				form = new Form("form", "Completed", null);
				form.addField(Field.fieldFixed("Script added."));
				response.getElements().add(form.getElement());

				response.completeSession();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new AdHocCommandException(Authorization.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@Override
	public String getName() {
		return "Add monitor task";
	}

	@Override
	public String getNode() {
		return "x-add-task";
	}

	@Override
	public boolean isAllowedFor(JID jid) {
		return component.isAdmin(jid);
	}

}
