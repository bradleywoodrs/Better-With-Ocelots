package bradleywoodrs.betterwithocelots.entities.ocelot.ai;

import bradleywoodrs.betterwithocelots.entities.ocelot.MobOcelot;

import java.util.ArrayList;
import java.util.List;

// ported from 1.2.5 deobf code (EntityAITasks/EntityAITaskEntry)
public class AITaskSelector {
	private final MobOcelot cat;
	private final List<AITask> tasks = new ArrayList<>();
	private final List<AITask> starting = new ArrayList<>();

	public AITaskSelector(MobOcelot cat) {
		this.cat = cat;
	}

	public <T extends AITask> T addTask(int priority, T task) {
		task.priority = priority;
		this.tasks.add(task);
		return task;
	}

	public void update() {
		this.starting.clear();
		for (AITask task : this.tasks) {
			if (task.running) {
				if (canUse(task) && task.continueExecuting()) {
					continue;
				}
				task.stop();
				task.running = false;
			}
			if (canUse(task) && task.shouldExecute()) {
				this.starting.add(task);
				task.running = true;
			}
		}
		for (AITask task : this.starting) {
			task.start();
		}
		for (AITask task : this.tasks) {
			if (task.running) {
				task.update();
			}
		}
	}

	private boolean canUse(AITask task) {
		if (this.cat.isPassenger() && !task.allowedWhileRiding()) return false;
		for (AITask other : this.tasks) {
			if (other == task || !other.running) continue;
			if (task.priority >= other.priority) {
				if ((task.mutex & other.mutex) != 0) return false;
			} else if (!other.interruptible()) {
				return false;
			}
		}
		return true;
	}
}
