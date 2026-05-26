package edu.ntnu.idi.idatt2003.view;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * A resizable Pane that renders a price-history line chart on an internal Canvas. Call {@link
 * #setData} to push fresh prices; the chart redraws automatically. Hovering near a data point shows
 * a tooltip with the week number and formatted price.
 */
public class PriceChart extends Pane {

  private static final Color COLOR_BG = Color.web("#0f1923");
  private static final Color COLOR_LINE = Color.web("#3b82f6");
  private static final Color COLOR_GRID = Color.web("#162030");
  private static final Color COLOR_SOFT = Color.web("#64748b");

  private final Canvas canvas;
  private final List<BigDecimal> prices;
  private final NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.US);

  private double[] xs = new double[0];
  private double[] ys = new double[0];

  /**
   * Creates a price chart pre-loaded with the given price history.
   *
   * @param initialPrices the historical prices to display
   * @param height the fixed height of the chart in pixels
   */
  public PriceChart(List<BigDecimal> initialPrices, double height) {
    this.prices = new ArrayList<>(initialPrices);

    canvas = new Canvas();
    getChildren().add(canvas);
    setPrefHeight(height);
    setMinHeight(80);
    setMaxWidth(Double.MAX_VALUE);

    Tooltip tip = new Tooltip();
    tip.setStyle(
        "-fx-background-color: #162030; -fx-text-fill: #e2e8f0;"
            + " -fx-font-size: 12px; -fx-padding: 4 8 4 8; -fx-background-radius: 4;");

    canvas.setOnMouseMoved(
        e -> {
          for (int i = 0; i < xs.length; i++) {
            if (Math.abs(e.getX() - xs[i]) < 8 && Math.abs(e.getY() - ys[i]) < 8) {
              tip.setText("Week " + (i + 1) + ":  " + fmt.format(prices.get(i)));
              tip.show(canvas, e.getScreenX() + 12, e.getScreenY() - 36);
              return;
            }
          }
          tip.hide();
        });
    canvas.setOnMouseExited(e -> tip.hide());
  }

  /**
   * Replaces the chart's price data and redraws if the canvas is ready.
   *
   * @param newPrices the new list of prices to display
   */
  public void setData(List<BigDecimal> newPrices) {
    prices.clear();
    prices.addAll(newPrices);
    if (canvas.getWidth() > 0) {
      redraw();
    }
  }

  @Override
  protected void layoutChildren() {
    double w = getWidth();
    double h = getHeight();
    if (w > 0 && h > 0) {
      canvas.setWidth(w);
      canvas.setHeight(h);
      redraw();
    }
  }

  private void redraw() {
    double w = canvas.getWidth();
    double h = canvas.getHeight();
    GraphicsContext gc = canvas.getGraphicsContext2D();

    gc.setFill(COLOR_BG);
    gc.fillRoundRect(0, 0, w, h, 8, 8);

    if (prices.size() < 2) {
      gc.setFill(COLOR_SOFT);
      gc.fillText("Not enough data yet", w / 2 - 60, h / 2 + 4);
      xs = new double[0];
      ys = new double[0];
      return;
    }

    double pad = 14;
    double cw = w - pad * 2;
    double ch = h - pad * 2;
    int n = prices.size();

    double min = prices.stream().mapToDouble(BigDecimal::doubleValue).min().orElse(0);
    double max = prices.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(1);
    double range = max - min == 0 ? 1 : max - min;

    xs = new double[n];
    ys = new double[n];
    for (int i = 0; i < n; i++) {
      xs[i] = pad + cw * i / (n - 1);
      ys[i] = pad + ch * (1 - (prices.get(i).doubleValue() - min) / range);
    }


    gc.setStroke(COLOR_GRID);
    gc.setLineWidth(1);
    for (int i = 1; i < 4; i++) {
      gc.strokeLine(pad, pad + ch * i / 4, w - pad, pad + ch * i / 4);
    }

    double[] areaXs = new double[n + 2];
    double[] areaYs = new double[n + 2];
    System.arraycopy(xs, 0, areaXs, 0, n);
    System.arraycopy(ys, 0, areaYs, 0, n);
    areaXs[n] = xs[n - 1];
    areaYs[n] = pad + ch;
    areaXs[n + 1] = xs[0];
    areaYs[n + 1] = pad + ch;
    gc.setFill(
        new LinearGradient(
            0,
            0,
            0,
            1,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#3b82f6", 0.25)),
            new Stop(1, Color.web("#3b82f6", 0.02))));
    gc.fillPolygon(areaXs, areaYs, n + 2);


    gc.setStroke(COLOR_LINE);
    gc.setLineWidth(2);
    gc.beginPath();
    gc.moveTo(xs[0], ys[0]);
    for (int i = 1; i < n; i++) {
      gc.lineTo(xs[i], ys[i]);
    }
    gc.stroke();


    gc.setFill(COLOR_LINE);
    for (int i = 0; i < n; i++) {
      gc.fillOval(xs[i] - 3, ys[i] - 3, 6, 6);
    }
  }
}
