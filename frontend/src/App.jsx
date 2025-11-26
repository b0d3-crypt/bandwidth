import { useState, useEffect, useRef } from "react";
import axios from "axios";
import { format } from "date-fns";
import { ptBR } from "date-fns/locale";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { Pause, Play } from "lucide-react";

const API_URL = "/api";
const POLL_INTERVAL = 5000;
const MIKROTIK_IP = "192.168.1.166";

function App() {
  const [isPaused, setIsPaused] = useState(false);
  const [interfaceName, setInterfaceName] = useState("ether1");
  const [data, setData] = useState([]);
  const [status, setStatus] = useState("Desconectado");
  const [lastReading, setLastReading] = useState(null);
  const [ipAddress, setIpAddress] = useState(MIKROTIK_IP);
  const intervalRef = useRef(null);

  const fetchBandwidth = async () => {
    try {
      const response = await axios.post(API_URL, {
        ipAddress: ipAddress,
        interfaceName: interfaceName,
      });

      const result = response.data;
      const timestamp = result.hora
        ? new Date(result.hora).getTime()
        : Date.now();
      const timeLabel = format(new Date(timestamp), "HH:mm:ss", {
        locale: ptBR,
      });

      const newDataPoint = {
        time: timeLabel,
        timestamp: timestamp,
        rx: parseFloat(result.rxMbps),
        tx: parseFloat(result.txMbps),
      };

      setData((prevData) => {
        const updated = [...prevData, newDataPoint];
        return updated.slice(-60);
      });

      setStatus("Conectado");
      setLastReading(timestamp);
    } catch (error) {
      console.error("Erro ao buscar dados:", error);
      setStatus("Desconectado");
    }
  };

  useEffect(() => {
    if (!isPaused) {
      fetchBandwidth();

      intervalRef.current = setInterval(() => {
        fetchBandwidth();
      }, POLL_INTERVAL);
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [isPaused, interfaceName, ipAddress]);

  const handlePauseToggle = () => {
    setIsPaused(!isPaused);
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }
  };

  const formatLastReading = () => {
    if (!lastReading) return "N/A";
    return format(new Date(lastReading), "HH:mm:ss", { locale: ptBR });
  };

  const maxY =
    data.length > 0 ? Math.max(...data.map((d) => Math.max(d.rx, d.tx))) : 0.05;

  const yAxisMax = maxY > 0 ? Math.max(maxY * 1.2, maxY + 0.005) : 0.05;

  const calculateTicks = (max) => {
    const adjustedMax = max * 1.2;
    let step;
    let decimals;

    if (max <= 0.01) {
      step = 0.002;
      decimals = 3;
    } else if (max <= 0.05) {
      step = 0.005;
      decimals = 3;
    } else if (max <= 0.1) {
      step = 0.01;
      decimals = 2;
    } else if (max <= 0.5) {
      step = 0.05;
      decimals = 2;
    } else {
      step = Math.ceil((adjustedMax / 5) * 20) / 20;
      decimals = 2;
    }

    const ticks = [];
    for (let i = 0; i <= adjustedMax + step / 2; i += step) {
      ticks.push(parseFloat(i.toFixed(decimals)));
    }
    return ticks;
  };

  const yAxisTicks = calculateTicks(maxY);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100 dark:from-slate-900 dark:to-slate-800 p-6">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-4xl font-bold text-center mb-8 text-slate-900 dark:text-slate-100">
          Mikrotik Bandwidth Monitor
        </h1>

        <div className="mb-6 text-center">
          <div className="inline-flex items-center gap-3 bg-white dark:bg-slate-800 rounded-lg px-6 py-3 shadow-md">
            <span className="text-sm text-slate-600 dark:text-slate-400 font-medium">
              IP/Hostname:{" "}
            </span>
            <input
              type="text"
              value={ipAddress}
              onChange={(e) => {
                setIpAddress(e.target.value);
                setData([]);
              }}
              className="text-lg font-semibold text-slate-900 dark:text-slate-100 bg-transparent border-b-2 border-slate-300 dark:border-slate-600 focus:border-blue-500 focus:outline-none px-2 py-1 min-w-[150px]"
              placeholder="192.168.1.166"
              disabled={!isPaused}
            />
          </div>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl shadow-xl p-6 mb-6">
          <ResponsiveContainer width="100%" height={500}>
            <LineChart
              data={data}
              margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
              <XAxis
                dataKey="time"
                stroke="#64748b"
                style={{ fontSize: "12px" }}
                interval="preserveStartEnd"
              />
              <YAxis
                domain={[0, yAxisMax]}
                ticks={yAxisTicks}
                label={{ value: "Mbps", angle: -90, position: "insideLeft" }}
                stroke="#64748b"
                style={{ fontSize: "12px" }}
                tickFormatter={(value) => value.toFixed(3)}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: "white",
                  border: "1px solid #e2e8f0",
                  borderRadius: "8px",
                  boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
                }}
                formatter={(value, name) => [
                  `${value} Mbps`,
                  name === "rx" ? "Rx" : "Tx",
                ]}
                labelFormatter={(label) => `Tempo: ${label}`}
              />
              <Legend
                formatter={(value) =>
                  value === "rx" ? "Rx (Recepção)" : "Tx (Transmissão)"
                }
              />
              <Line
                type="monotone"
                dataKey="rx"
                stroke="#3b82f6"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 6 }}
                name="rx"
              />
              <Line
                type="monotone"
                dataKey="tx"
                stroke="#10b981"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 6 }}
                name="tx"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl shadow-xl p-6 mb-6">
          <div className="flex flex-col sm:flex-row gap-4 items-center justify-center">
            <div className="flex items-center gap-3">
              <label
                htmlFor="interface"
                className="text-sm font-medium text-slate-700 dark:text-slate-300"
              >
                Interface:
              </label>
              <select
                id="interface"
                value={interfaceName}
                onChange={(e) => {
                  setInterfaceName(e.target.value);
                  setData([]);
                }}
                className="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                disabled={!isPaused}
              >
                <option value="ether1">ether1</option>
                <option value="ether2">ether2</option>
              </select>
            </div>

            <button
              onClick={handlePauseToggle}
              className={`flex items-center gap-2 px-6 py-2 rounded-lg font-medium transition-all duration-200 ${
                isPaused
                  ? "bg-green-500 hover:bg-green-600 text-white"
                  : "bg-red-500 hover:bg-red-600 text-white"
              }`}
            >
              {isPaused ? (
                <>
                  <Play size={18} />
                  Retomar
                </>
              ) : (
                <>
                  <Pause size={18} />
                  Pausar
                </>
              )}
            </button>
          </div>
        </div>

        <div className="bg-white dark:bg-slate-800 rounded-xl shadow-xl p-4">
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 text-sm">
            <div className="flex items-center gap-2">
              <span className="text-slate-600 dark:text-slate-400 font-medium">
                Status:
              </span>
              <span
                className={`font-semibold ${
                  status === "Conectado"
                    ? "text-green-600 dark:text-green-400"
                    : "text-red-600 dark:text-red-400"
                }`}
              >
                {status}
              </span>
              {status === "Conectado" && (
                <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
              )}
            </div>
            <div className="flex items-center gap-2">
              <span className="text-slate-600 dark:text-slate-400 font-medium">
                Última leitura:
              </span>
              <span className="text-slate-900 dark:text-slate-100 font-mono">
                {formatLastReading()}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
