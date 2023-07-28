package fr.theoszanto.mc.crateexpress.exporters;

import fr.theoszanto.mc.crateexpress.CrateExpress;
import fr.theoszanto.mc.crateexpress.models.Crate;
import fr.theoszanto.mc.crateexpress.models.reward.CrateReward;
import fr.theoszanto.mc.crateexpress.utils.FormatUtils;
import fr.theoszanto.mc.express.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

public class CSVCrateExporter extends CrateExporter {
	private static final char SEPARATOR = ',';
	private static final char DELIMITOR = '\n';
	private static final char ENCLOSURE = '"';
	private static final char ESCAPE = '\\';

	public CSVCrateExporter(@NotNull CrateExpress plugin) {
		super(plugin, "csv");
	}

	private static final @NotNull String DATA_HEADER = "Type" + SEPARATOR + "Weight" + SEPARATOR + "Chance" + SEPARATOR + "Name" + SEPARATOR + "Item" + SEPARATOR + ENCLOSURE + "Additional data" + ENCLOSURE + DELIMITOR;

	// TODO Improve CSV export
	@Override
	public void export(@NotNull Crate crate, @NotNull Writer writer) throws IOException {
		double crateWeight = crate.totalWeight();
		writer.write(DATA_HEADER);
		for (CrateReward reward : crate) {
			double rewardWeight = reward.getWeight();
			// Type
			writer.write(rewardType(reward));
			writer.write(SEPARATOR);
			// Weight
			writer.write(FormatUtils.noTrailingZeroDecimal(rewardWeight));
			writer.write(SEPARATOR);
			// Chance
			writer.write(FormatUtils.noTrailingZeroDecimal(MathUtils.round(100 * rewardWeight / crateWeight, 2)));
			writer.write(SEPARATOR);
			// Name
			writer.write(ENCLOSURE);
			writer.write(reward.describe());
			writer.write(ENCLOSURE);
			writer.write(SEPARATOR);
			// Item
			writer.write(reward.getIcon().getType().name());
			writer.write(SEPARATOR);
			// Additional data
			writer.write(ENCLOSURE);
//			writer.write("");
			writer.write(ENCLOSURE);
			writer.write(DELIMITOR);
		}
	}

	private static final @NotNull String TYPE_PREFIX = "crate";
	private static final @NotNull String TYPE_SUFFIX = "reward";

	private static @NotNull String rewardType(@NotNull CrateReward reward) {
		String name = reward.getClass().getSimpleName().toLowerCase();
		if (name.startsWith(TYPE_PREFIX))
			name = name.substring(TYPE_PREFIX.length());
		if (name.endsWith(TYPE_SUFFIX))
			name = name.substring(0, name.length() - TYPE_SUFFIX.length());
		return name;
	}
}
