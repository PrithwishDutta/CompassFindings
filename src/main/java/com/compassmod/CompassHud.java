package com.compassmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class CompassHud {

    public enum Mode { SPAWN, PLAYER }

    private static Mode currentMode = Mode.SPAWN;
    private static final List<String> trackedPlayers = new ArrayList<>();

    public static void cycleMode() {
        currentMode = (currentMode == Mode.SPAWN) ? Mode.PLAYER : Mode.SPAWN;
    }

    public static String getModeName() {
        return switch (currentMode) {
            case SPAWN  -> "Pointing to Spawn";
            case PLAYER -> "Pointing to Player(s)";
        };
    }

    public static void addTrackedPlayer(String name) {
        if (!trackedPlayers.contains(name)) trackedPlayers.add(name);
    }

    public static void removeTrackedPlayer(String name) {
        trackedPlayers.remove(name);
    }

    public static List<String> getTrackedPlayers() { return trackedPlayers; }

    // ── Render ─────────────────────────────────────────────────────────────
    public static void render(GuiGraphics ctx, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;

        LocalPlayer player = mc.player;
        double px = player.getX();
        double pz = player.getZ();
        float yaw = player.getViewYRot(tickDelta);

        switch (currentMode) {
            case SPAWN  -> renderCompassWidget(ctx, mc, px, pz, yaw, 0, 0, "Spawn", 0xFFFFAA00);
            case PLAYER -> renderPlayerTrackers(ctx, mc, px, pz, yaw, tickDelta);
        }
    }

    private static final int WIDGET_X = 8;
    private static final int WIDGET_Y = 8;
    private static final int RADIUS   = 22;
    private static final int TOTAL_W  = (RADIUS + 4) * 2;
    private static final int TOTAL_H  = (RADIUS + 4) * 2 + 20;

    private static void renderCompassWidget(GuiGraphics ctx, Minecraft mc,
                                            double px, double pz, float yaw,
                                            double targetX, double targetZ,
                                            String label, int needleColor) {
        int cx = WIDGET_X + RADIUS + 4;
        int cy = WIDGET_Y + RADIUS + 4;

        ctx.fill(WIDGET_X, WIDGET_Y, WIDGET_X + TOTAL_W, WIDGET_Y + TOTAL_H, 0xAA000000);
        drawBorder(ctx, WIDGET_X, WIDGET_Y, WIDGET_X + TOTAL_W, WIDGET_Y + TOTAL_H, 0xFF888888);
        drawCircle(ctx, cx, cy, RADIUS, 0xFF333333);
        drawCircle(ctx, cx, cy, RADIUS - 1, 0xFF555555);

        int cc = 0xFFCCCCCC;
        ctx.drawCenteredString(mc.font, "N", cx,              cy - RADIUS + 2,  0xFFFF4444);
        ctx.drawCenteredString(mc.font, "S", cx,              cy + RADIUS - 10, cc);
        ctx.drawCenteredString(mc.font, "W", cx - RADIUS + 2, cy - 3,           cc);
        ctx.drawCenteredString(mc.font, "E", cx + RADIUS - 6, cy - 3,           cc);

        double dx = targetX - px;
        double dz = targetZ - pz;
        double worldAngle = Math.toDegrees(Math.atan2(dz, dx)) + 90.0;
        double needleAngle = Math.toRadians(worldAngle - yaw);

        float needleLen = RADIUS - 4;
        int nx = cx + (int)(Math.sin(needleAngle) * needleLen);
        int ny = cy - (int)(Math.cos(needleAngle) * needleLen);
        drawLine(ctx, cx, cy, nx, ny, needleColor);
        int tx = cx - (int)(Math.sin(needleAngle) * (needleLen * 0.4f));
        int ty = cy + (int)(Math.cos(needleAngle) * (needleLen * 0.4f));
        drawLine(ctx, cx, cy, tx, ty, 0xFF888888);
        ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);

        double dist = Math.sqrt(dx * dx + dz * dz);
        String distStr = dist < 1000
                ? String.format("%.0fm", dist)
                : String.format("%.1fkm", dist / 1000.0);
        ctx.drawCenteredString(mc.font, label,   cx, WIDGET_Y + TOTAL_H - 18, 0xFFFFDD44);
        ctx.drawCenteredString(mc.font, distStr, cx, WIDGET_Y + TOTAL_H - 8,  0xFFAAAAAA);
    }

    private static void renderPlayerTrackers(GuiGraphics ctx, Minecraft mc,
                                             double px, double pz, float yaw,
                                             float tickDelta) {
        List<String> targets = new ArrayList<>(trackedPlayers);

        if (targets.isEmpty()) {
            for (AbstractClientPlayer p : mc.level.players()) {
                if (!p.equals(mc.player)) targets.add(p.getName().getString());
            }
        }

        if (targets.isEmpty()) {
            int cx = WIDGET_X + RADIUS + 4;
            ctx.fill(WIDGET_X, WIDGET_Y, WIDGET_X + TOTAL_W, WIDGET_Y + TOTAL_H, 0xAA000000);
            drawBorder(ctx, WIDGET_X, WIDGET_Y, WIDGET_X + TOTAL_W, WIDGET_Y + TOTAL_H, 0xFF888888);
            drawCircle(ctx, cx, WIDGET_Y + RADIUS + 4, RADIUS, 0xFF333333);
            ctx.drawCenteredString(mc.font, "No players", cx, WIDGET_Y + TOTAL_H - 18, 0xFF888888);
            return;
        }

        int[] colors = {0xFF00FF88, 0xFF00AAFF, 0xFFFF88FF, 0xFFFFAA00, 0xFFFF4444};
        int offsetX = 0;

        for (int i = 0; i < Math.min(targets.size(), 4); i++) {
            String targetName = targets.get(i);
            double tx = 0, tz = 0;
            boolean found = false;

            for (AbstractClientPlayer p : mc.level.players()) {
                if (p.getName().getString().equals(targetName)) {
                    tx = Mth.lerp(tickDelta, p.xOld, p.getX());
                    tz = Mth.lerp(tickDelta, p.zOld, p.getZ());
                    found = true;
                    break;
                }
            }
            if (!found) continue;

            int color = colors[i % colors.length];
            int wx = WIDGET_X + offsetX;
            int cx = wx + RADIUS + 4;
            int cy = WIDGET_Y + RADIUS + 4;

            ctx.fill(wx, WIDGET_Y, wx + TOTAL_W, WIDGET_Y + TOTAL_H, 0xAA000000);
            drawBorder(ctx, wx, WIDGET_Y, wx + TOTAL_W, WIDGET_Y + TOTAL_H, (color & 0x00FFFFFF) | 0x88000000);
            drawCircle(ctx, cx, cy, RADIUS, 0xFF333333);

            int cc = 0xFFCCCCCC;
            ctx.drawCenteredString(mc.font, "N", cx,              cy - RADIUS + 2,  0xFFFF4444);
            ctx.drawCenteredString(mc.font, "S", cx,              cy + RADIUS - 10, cc);
            ctx.drawCenteredString(mc.font, "W", cx - RADIUS + 2, cy - 3,           cc);
            ctx.drawCenteredString(mc.font, "E", cx + RADIUS - 6, cy - 3,           cc);

            double dx = tx - px;
            double dz = tz - pz;
            double worldAngle = Math.toDegrees(Math.atan2(dz, dx)) + 90.0;
            double needleAngle = Math.toRadians(worldAngle - yaw);

            float needleLen = RADIUS - 4;
            int nx = cx + (int)(Math.sin(needleAngle) * needleLen);
            int ny = cy - (int)(Math.cos(needleAngle) * needleLen);
            drawLine(ctx, cx, cy, nx, ny, color);
            int tailX = cx - (int)(Math.sin(needleAngle) * (needleLen * 0.4f));
            int tailY = cy + (int)(Math.cos(needleAngle) * (needleLen * 0.4f));
            drawLine(ctx, cx, cy, tailX, tailY, 0xFF888888);
            ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);

            double dist = Math.sqrt(dx * dx + dz * dz);
            String distStr = dist < 1000
                    ? String.format("%.0fm", dist)
                    : String.format("%.1fkm", dist / 1000.0);
            String shortName = targetName.length() > 8 ? targetName.substring(0, 7) + "." : targetName;
            ctx.drawCenteredString(mc.font, shortName, cx, WIDGET_Y + TOTAL_H - 18, color);
            ctx.drawCenteredString(mc.font, distStr,   cx, WIDGET_Y + TOTAL_H - 8,  0xFFAAAAAA);

            offsetX += TOTAL_W + 2;
        }
    }

    private static void drawCircle(GuiGraphics ctx, int cx, int cy, int r, int color) {
        int x = 0, y = r, d = 1 - r;
        while (x <= y) {
            plotCirclePoints(ctx, cx, cy, x, y, color);
            if (d < 0) d += 2 * x + 3;
            else { d += 2 * (x - y) + 5; y--; }
            x++;
        }
    }

    private static void plotCirclePoints(GuiGraphics ctx, int cx, int cy, int x, int y, int color) {
        int[][] pts = {{cx+x,cy+y},{cx-x,cy+y},{cx+x,cy-y},{cx-x,cy-y},
                       {cx+y,cy+x},{cx-y,cy+x},{cx+y,cy-x},{cx-y,cy-x}};
        for (int[] p : pts) ctx.fill(p[0], p[1], p[0]+1, p[1]+1, color);
    }

    private static void drawLine(GuiGraphics ctx, int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1-x0), dy = Math.abs(y1-y0);
        int sx = x0<x1?1:-1, sy = y0<y1?1:-1, err = dx-dy;
        while (true) {
            ctx.fill(x0, y0, x0+1, y0+1, color);
            if (x0==x1 && y0==y1) break;
            int e2 = 2*err;
            if (e2>-dy){err-=dy; x0+=sx;}
            if (e2< dx){err+=dx; y0+=sy;}
        }
    }

    private static void drawBorder(GuiGraphics ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y1+1, color);
        ctx.fill(x1, y2-1, x2, y2, color);
        ctx.fill(x1, y1, x1+1, y2, color);
        ctx.fill(x2-1, y1, x2, y2, color);
    }
}
